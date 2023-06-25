package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.libcrypto.Curve25519
import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.components.AuthComponent
import dev.medzik.librepass.server.components.RequestIP
import dev.medzik.librepass.server.components.TokenType
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.services.EmailService
import dev.medzik.librepass.server.utils.*
import dev.medzik.librepass.types.api.auth.*
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// The server's key pair is used for authentication using a shared key.
//
// It is not required that the key pair be the same all the time, so it
// is generated when the server is started and each time it is restarted,
// the key is different.
val ServerKeyPair = Curve25519.generateKeyPair()!!

/**
 * AuthController handles the authentication process.
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController @Autowired constructor(
    private val userRepository: UserRepository,
    private val authComponent: AuthComponent,
    private val emailService: EmailService,
    @Value("\${server.api.rateLimit.enabled}")
    private val rateLimitEnabled: Boolean,
    @Value("\${email.verification.required}")
    private val emailVerificationRequired: Boolean
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // coroutine scope
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Rate limit for login endpoint per IP address.
     */
    class AuthRateLimitConfig {
        private var cache: Map<String, Bucket> = ConcurrentHashMap()

        fun resolveBucket(key: String): Bucket {
            return cache[key] ?: newBucket().also { cache += key to it }
        }

        private fun newBucket(): Bucket {
            return Bucket.builder()
                .addLimit(Bandwidth.classic(20, Refill.intervally(10, Duration.ofMinutes(1))))
                .build()
        }
    }

    // rate limit for login endpoint
    private val rateLimit = AuthRateLimitConfig()

    @PostMapping("/register")
    fun register(
        @RequestIP ip: String,
        @RequestBody request: RegisterRequest
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TOO_MANY_REQUESTS.toResponse()

        // compute shared key
        val sharedKey = Curve25519.computeSharedSecret(ServerKeyPair.privateKey, request.publicKey)

        // validate shared key
        if (request.sharedKey != sharedKey)
            return ResponseError.INVALID_CREDENTIALS.toResponse()

        val verificationToken = UUID.randomUUID()

        val user = UserTable(
            email = request.email,
            passwordHint = request.passwordHint,
            // Argon2id parameters
            parallelism = request.parallelism,
            memory = request.memory,
            iterations = request.iterations,
            version = request.version,
            // Curve25519 key pair
            publicKey = request.publicKey,
            // Email verification token
            emailVerificationCode = verificationToken,
            emailVerificationCodeExpiresAt = Date.from(
                Calendar.getInstance().apply {
                    add(Calendar.HOUR, 24)
                }.toInstant()
            )
        )

        // save user to database
        userRepository.save(user)

        // send email verification
        scope.launch {
            try {
                emailService.sendEmailVerification(
                    to = request.email,
                    user = user.id.toString(),
                    code = user.emailVerificationCode.toString()
                )
            } catch (e: Exception) {
                logger.error("Failed to send email verification", e)
            }
        }

        return ResponseHandler.generateResponse(HttpStatus.CREATED)
    }

    @GetMapping("/userArgon2Parameters")
    fun getUserArgon2Parameters(
        @RequestIP ip: String,
        @RequestParam("email") email: String
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TOO_MANY_REQUESTS.toResponse()

        // check if email is empty
        if (email.isEmpty())
            return ResponseError.INVALID_CREDENTIALS.toResponse()

        // get user from database
        val user = userRepository.findByEmail(email)
            ?: return ResponseError.INVALID_CREDENTIALS.toResponse()

        val argon2Parameters = UserArgon2idParameters(
            parallelism = user.parallelism,
            memory = user.memory,
            iterations = user.iterations,
            version = user.version
        )

        return ResponseHandler.generateResponse(argon2Parameters, HttpStatus.OK)
    }

    @GetMapping("/serverPublicKey")
    fun getServerPublicKey(): Response {
        val response = ServerPublicKey(
            publicKey = ServerKeyPair.publicKey
        )

        return ResponseHandler.generateResponse(response, HttpStatus.OK)
    }

    @PostMapping("/login")
    fun login(
        @RequestIP ip: String,
        @RequestBody request: LoginRequest
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TOO_MANY_REQUESTS.toResponse()

        // get user from database
        val user = userRepository.findByEmail(request.email)
            ?: return ResponseError.INVALID_CREDENTIALS.toResponse()

        // check if email is verified
        if (emailVerificationRequired && !user.emailVerified)
            return ResponseError.EMAIL_NOT_VERIFIED.toResponse()

        // compute shared key
        val sharedKey = Curve25519.computeSharedSecret(ServerKeyPair.privateKey, user.publicKey)

        // validate shared key
        if (request.sharedKey != sharedKey)
            return ResponseError.INVALID_CREDENTIALS.toResponse()

        // prepare response
        val credentials = LoginResponse(
            userId = user.id,
            apiKey = authComponent.generateToken(TokenType.API_KEY, user.id)
        )

        return ResponseHandler.generateResponse(credentials, HttpStatus.OK)
    }

    @GetMapping("/passwordHint")
    fun requestPasswordHint(
        @RequestIP ip: String,
        @RequestParam("email") email: String?
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TOO_MANY_REQUESTS.toResponse()

        if (email == null)
            return ResponseError.INVALID_BODY.toResponse()

        // get user from database
        val user = userRepository.findByEmail(email)
            ?: return ResponseError.INVALID_CREDENTIALS.toResponse()

        try {
            emailService.sendPasswordHint(
                to = user.email,
                hint = user.passwordHint
            )
        } catch (e: Exception) {
            logger.error("Failed to send password hint", e)
        }

        return ResponseHandler.generateResponse(HttpStatus.OK)
    }

    @GetMapping("/verifyEmail")
    fun verifyEmail(
        @RequestParam("user") userID: String,
        @RequestParam("code") verificationCode: String
    ): Response {
        // get user from database
        val user = userRepository.findById(UUID.fromString(userID)).orElse(null)
            ?: return ResponseError.INVALID_BODY.toResponse()

        // check if the code is valid
        if (user.emailVerificationCode.toString() != verificationCode)
            return ResponseError.INVALID_BODY.toResponse()

        // check if the code is expired
        if (user.emailVerificationCodeExpiresAt?.before(Date()) == true)
            return ResponseError.INVALID_BODY.toResponse()

        // check if user email is already verified
        if (user.emailVerified)
            return ResponseError.INVALID_BODY.toResponse()

        // set email as verified
        userRepository.save(
            user.copy(emailVerified = true)
        )

        return ResponseHandler.generateResponse(HttpStatus.OK)
    }
}
