package dev.medzik.librepass.server.controllers.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dev.medzik.libcrypto.Curve25519
import dev.medzik.libcrypto.Curve25519KeyPair
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.components.RequestIP
import dev.medzik.librepass.server.database.TokenRepository
import dev.medzik.librepass.server.database.TokenTable
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.services.EmailService
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.server.utils.Validator
import dev.medzik.librepass.server.utils.toResponse
import dev.medzik.librepass.types.api.auth.*
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.codec.binary.Hex
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
val ServerKeyPair: Curve25519KeyPair = Curve25519.generateKeyPair()

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

@RestController
@RequestMapping("/api/auth")
class AuthController @Autowired constructor(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    private val emailService: EmailService,
    @Value("\${server.api.rateLimit.enabled}")
    private val rateLimitEnabled: Boolean,
    @Value("\${email.verification.required}")
    private val emailVerificationRequired: Boolean
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rateLimit = AuthRateLimitConfig()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @PostMapping("/register")
    fun register(
        @RequestIP ip: String,
        @RequestBody request: RegisterRequest
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TOO_MANY_REQUESTS.toResponse()

        if (!Validator.emailValidator(request.email) ||
            !Validator.hexValidator(request.sharedKey, 32) ||
            !Validator.hexValidator(request.publicKey, 32) ||
            request.parallelism < 0 ||
            request.memory < 19 * 1024 ||
            request.iterations < 0 ||
            request.version != 19
        ) return ResponseError.INVALID_BODY.toResponse()

        val sharedKey = Curve25519.computeSharedSecret(ServerKeyPair.privateKey, request.publicKey)
        if (request.sharedKey != sharedKey)
            return ResponseError.INVALID_CREDENTIALS.toResponse()

        val verificationToken = Hex.encodeHexString(Salt.generate(16))

        val user = userRepository.save(
            UserTable(
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
        )

        coroutineScope.launch {
            try {
                emailService.sendEmailVerification(
                    to = request.email,
                    user = user.id.toString(),
                    code = user.emailVerificationCode.toString()
                )
            } catch (e: Throwable) {
                logger.error("Error sending email verification", e)
            }
        }

        return ResponseHandler.generateResponse(HttpStatus.CREATED)
    }

    @GetMapping("/preLogin")
    fun preLogin(
        @RequestIP ip: String,
        @RequestParam("email") email: String?
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TOO_MANY_REQUESTS.toResponse()

        if (email.isNullOrEmpty())
            return ResponseHandler.generateResponse(
                PreLoginResponse(
                    // Default argon2id parameters
                    parallelism = 3,
                    memory = 65536, // 64MB
                    iterations = 4,
                    version = 19,
                    // Server Curve25519 public key
                    serverPublicKey = ServerKeyPair.publicKey
                ),
                HttpStatus.OK
            )

        val user = userRepository.findByEmail(email)
            ?: return ResponseError.INVALID_CREDENTIALS.toResponse()

        return ResponseHandler.generateResponse(
            PreLoginResponse(
                parallelism = user.parallelism,
                memory = user.memory,
                iterations = user.iterations,
                version = user.version,
                serverPublicKey = ServerKeyPair.publicKey
            ),
            HttpStatus.OK
        )
    }

    @PostMapping("/oauth")
    fun auth(
        @RequestIP ip: String,
        @RequestParam("grantType") grantType: String,
        @RequestBody request: String
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TOO_MANY_REQUESTS.toResponse()

        when (grantType) {
            "login" -> {
                try {
                    return oauthLogin(
                        ip = ip,
                        request = Gson().fromJson(request, LoginRequest::class.java)
                    )
                } catch (e: JsonSyntaxException) {
                    ResponseError.INVALID_CREDENTIALS.toResponse()
                }
            }
//            "2fa" -> {
//                return if (request is TwoFactorRequest)
//                    oauth2FA(request)
//                else
//                    ResponseError.INVALID_BODY.toResponse()
//            }
        }

        return ResponseError.INVALID_BODY.toResponse()
    }

    private fun oauthLogin(ip: String, request: LoginRequest): Response {
        val user = userRepository.findByEmail(request.email)
            ?: return ResponseError.INVALID_CREDENTIALS.toResponse()

        if (emailVerificationRequired && !user.emailVerified)
            return ResponseError.EMAIL_NOT_VERIFIED.toResponse()

        val sharedKey = Curve25519.computeSharedSecret(ServerKeyPair.privateKey, user.publicKey)
        if (request.sharedKey != sharedKey)
            return ResponseError.INVALID_CREDENTIALS.toResponse()

        val apiToken = tokenRepository.save(
            TokenTable(
                owner = user.id,
                lastIp = ip
            )
        )

        return ResponseHandler.generateResponse(
            UserCredentialsResponse(
                userId = user.id,
                apiKey = apiToken.token
            )
        )
    }

//    private fun oauth2FA(request: TwoFactorRequest): Response {
//        return ResponseHandler.generateResponse(HttpStatus.NOT_IMPLEMENTED)
//    }

    @GetMapping("/passwordHint")
    fun requestPasswordHint(
        @RequestIP ip: String,
        @RequestParam("email") email: String?
    ): Response {
        if (rateLimitEnabled && !rateLimit.resolveBucket(ip).tryConsume(1))
            return ResponseError.TOO_MANY_REQUESTS.toResponse()

        if (email == null)
            return ResponseError.INVALID_BODY.toResponse()

        val user = userRepository.findByEmail(email)
            ?: return ResponseError.INVALID_CREDENTIALS.toResponse()

        try {
            emailService.sendPasswordHint(
                to = user.email,
                hint = user.passwordHint
            )
        } catch (e: Exception) {
            logger.error("Failed to send password hint", e)

            return ResponseError.UNEXPECTED_SERVER_ERROR.toResponse()
        }

        return ResponseHandler.generateResponse(HttpStatus.OK)
    }

    @GetMapping("/verifyEmail")
    fun verifyEmail(
        @RequestParam("user") userID: String,
        @RequestParam("code") verificationCode: String
    ): Response {
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
