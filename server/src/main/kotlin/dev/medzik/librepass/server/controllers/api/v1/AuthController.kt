package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.libcrypto.Argon2HashingFunction
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.server.components.AuthComponent
import dev.medzik.librepass.server.components.RequestIP
import dev.medzik.librepass.server.components.TokenType
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.services.EmailService
import dev.medzik.librepass.server.utils.*
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import dev.medzik.librepass.types.api.auth.UserCredentials
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

@RestController
@RequestMapping("/api/v1/auth")
class AuthController @Autowired constructor(
    private val userRepository: UserRepository,
    private val authComponent: AuthComponent,
    private val emailService: EmailService,
    @Value("\${librepass.api.rateLimit.enabled}")
    private val rateLimitEnabled: Boolean
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

    /**
     * Register new user.
     */
    @PostMapping("/register")
    fun register(
        @RequestIP ip: String,
        @RequestBody request: RegisterRequest
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TooManyRequests

        val passwordSalt = Salt.generate(32)
        val passwordHash = Argon2DefaultHasher.hash(request.password, passwordSalt).toString()

        val verificationToken = UUID.randomUUID()

        val user = UserTable(
            email = request.email,
            password = passwordHash,
            passwordHint = request.passwordHint,
            encryptionKey = request.encryptionKey,
            // argon2id parameters
            parallelism = request.parallelism,
            memory = request.memory,
            iterations = request.iterations,
            version = request.version,
            // RSA keypair
            publicKey = request.publicKey,
            privateKey = request.privateKey,
            // email verification
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

    /**
     * Get argon2id parameters for user. Used for client-side password hashing.
     * @return [UserArgon2idParameters]
     */
    @GetMapping("/userArgon2Parameters")
    fun getUserArgon2Parameters(
        @RequestIP ip: String,
        @RequestParam("email") email: String
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TooManyRequests

        // check if email is empty
        if (email.isEmpty())
            return ResponseError.InvalidCredentials

        // get user from database
        val user = userRepository.findByEmail(email)
            ?: return ResponseError.InvalidCredentials

        val argon2Parameters = UserArgon2idParameters(
            parallelism = user.parallelism,
            memory = user.memory,
            iterations = user.iterations,
            version = user.version
        )

        return ResponseHandler.generateResponse(argon2Parameters, HttpStatus.OK)
    }

    /**
     * Login user.
     * @return [UserCredentials]
     */
    @PostMapping("/login")
    fun login(
        @RequestIP ip: String,
        @RequestBody request: LoginRequest
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TooManyRequests

        // check if email or password is empty
        if (request.email.isEmpty() || request.password.isEmpty())
            return ResponseError.InvalidBody

        // get user from database
        val user = userRepository.findByEmail(request.email)
            ?: return ResponseError.InvalidCredentials

        // check if password is correct
        if (!Argon2HashingFunction.verify(request.password, user.password))
            return ResponseError.InvalidCredentials

        // prepare response
        val credentials = UserCredentials(
            userId = user.id,
            accessToken = authComponent.generateToken(TokenType.ACCESS_TOKEN, user.id),
            encryptionKey = user.encryptionKey
        )

        return ResponseHandler.generateResponse(credentials, HttpStatus.OK)
    }

    /**
     * Request password hint.
     */
    @GetMapping("/passwordHint")
    fun requestPasswordHint(
        @RequestIP ip: String,
        @RequestParam("email") email: String
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TooManyRequests

        // get user from database
        val user = userRepository.findByEmail(email)
            ?: return ResponseError.InvalidCredentials

        try {
            emailService.sendPasswordHint(
                to = user.email,
                hint = user.passwordHint
            )
        } catch (e: Exception) {
            logger.error("Failed to send password hint", e)
        }

        return ResponseSuccess.OK
    }

    /**
     * Verify email address. This endpoint is called when user clicks on the link in the email.
     */
    @GetMapping("/verifyEmail")
    fun verifyEmail(
        @RequestParam("user") userID: String,
        @RequestParam("code") verificationCode: String
    ): Response {
        // get user from database
        val user = userRepository.findById(UUID.fromString(userID)).orElse(null)
            ?: return ResponseError.InvalidBody

        // check if code is valid
        if (user.emailVerificationCode.toString() != verificationCode)
            return ResponseError.InvalidBody

        // check if code is expired
        if (user.emailVerificationCodeExpiresAt?.before(Date()) == true)
            return ResponseError.InvalidBody

        // check if user email is already verified
        if (user.emailVerified)
            return ResponseError.InvalidBody

        // set email as verified
        userRepository.save(
            user.copy(emailVerified = true)
        )

        return ResponseSuccess.OK
    }
}
