package dev.medzik.librepass.server.controllers.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dev.medzik.libcrypto.Random
import dev.medzik.libcrypto.X25519
import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.components.RequestIP
import dev.medzik.librepass.server.controllers.advice.AuthorizedUserException
import dev.medzik.librepass.server.controllers.advice.InvalidTwoFactorCodeException
import dev.medzik.librepass.server.controllers.advice.RateLimitException
import dev.medzik.librepass.server.database.TokenRepository
import dev.medzik.librepass.server.database.TokenTable
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.ratelimit.AuthControllerEmailRateLimitConfig
import dev.medzik.librepass.server.ratelimit.AuthControllerRateLimitConfig
import dev.medzik.librepass.server.ratelimit.BaseRateLimitConfig
import dev.medzik.librepass.server.services.EmailService
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.server.utils.Validator
import dev.medzik.librepass.server.utils.toResponse
import dev.medzik.librepass.types.api.*
import dev.medzik.librepass.utils.TOTP
import dev.medzik.librepass.utils.fromHexString
import dev.medzik.librepass.utils.toHexString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

// The server's key pair is used for authentication using a shared key.
//
// It is not required that the key pair be the same all the time, so it
// is generated when the server is started and each time it is restarted,
// the key is different.
val ServerPrivateKey: ByteArray = X25519.generatePrivateKey()
val ServerPublicKey: ByteArray = X25519.publicFromPrivate(ServerPrivateKey)

@RestController
@RequestMapping("/api/auth")
class AuthController
    @Autowired
    constructor(
        private val userRepository: UserRepository,
        private val tokenRepository: TokenRepository,
        private val emailService: EmailService,
        @Value("\${server.api.rateLimit.enabled}")
        private val rateLimitEnabled: Boolean,
        @Value("\${email.verification.required}")
        private val emailVerificationRequired: Boolean,
        @Value("\${web.url}")
        private val webUrl: String
    ) {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private val rateLimit = AuthControllerRateLimitConfig()
        private val rateLimitEmail = AuthControllerEmailRateLimitConfig()
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        @PostMapping("/register")
        fun register(
            @RequestIP ip: String,
            @RequestBody request: RegisterRequest
        ): Response {
            consumeRateLimit(ip)

            if (!Validator.emailValidator(request.email) ||
                !Validator.hexValidator(request.sharedKey, 32) ||
                !Validator.hexValidator(request.publicKey, 32) ||
                request.parallelism < 0 ||
                request.memory < 19 * 1024 ||
                request.iterations < 0
            )
                return ResponseError.INVALID_BODY.toResponse()

            val sharedKey = X25519.computeSharedSecret(ServerPrivateKey, request.publicKey.fromHexString())
            if (!request.sharedKey.fromHexString().contentEquals(sharedKey))
                return ResponseError.INVALID_CREDENTIALS.toResponse()

            val verificationToken = Random.randBytes(16).toHexString()

            val user =
                userRepository.save(
                    UserTable(
                        email = request.email.lowercase(),
                        passwordHint = request.passwordHint,
                        // Argon2id parameters
                        parallelism = request.parallelism,
                        memory = request.memory,
                        iterations = request.iterations,
                        // Curve25519 key pair
                        publicKey = request.publicKey,
                        // Email verification token
                        emailVerificationCode = verificationToken,
                        emailVerificationCodeExpiresAt =
                            Date.from(
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
            consumeRateLimit(ip)

            if (email.isNullOrEmpty())
                return ResponseHandler.generateResponse(
                    PreLoginResponse(
                        // Default argon2id parameters
                        parallelism = 3,
                        memory = 65536,
                        iterations = 4,
                        // Server Curve25519 public key
                        serverPublicKey = ServerPublicKey.toHexString()
                    ),
                    HttpStatus.OK
                )

            val user =
                userRepository.findByEmail(email.lowercase())
                    ?: return ResponseError.INVALID_CREDENTIALS.toResponse()

            return ResponseHandler.generateResponse(
                PreLoginResponse(
                    parallelism = user.parallelism,
                    memory = user.memory,
                    iterations = user.iterations,
                    // Server Curve25519 public key
                    serverPublicKey = ServerPublicKey.toHexString()
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
            consumeRateLimit(ip)

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
                "2fa" -> {
                    try {
                        return oauth2FA(
                            request = Gson().fromJson(request, TwoFactorRequest::class.java)
                        )
                    } catch (e: JsonSyntaxException) {
                        ResponseError.INVALID_CREDENTIALS.toResponse()
                    }
                }
            }

            return ResponseError.INVALID_BODY.toResponse()
        }

        private fun oauthLogin(
            ip: String,
            request: LoginRequest
        ): Response {
            consumeRateLimit(request.email.lowercase())

            val user =
                userRepository.findByEmail(request.email.lowercase())
                    ?: return ResponseError.INVALID_CREDENTIALS.toResponse()

            if (emailVerificationRequired && !user.emailVerified)
                return ResponseError.EMAIL_NOT_VERIFIED.toResponse()

            val sharedKey = X25519.computeSharedSecret(ServerPrivateKey, user.publicKey.fromHexString())
            if (!request.sharedKey.fromHexString().contentEquals(sharedKey))
                return ResponseError.INVALID_CREDENTIALS.toResponse()

            val apiToken =
                tokenRepository.save(
                    TokenTable(
                        owner = user.id,
                        lastIp = ip,
                        // Allow use of an api key only if two-factor authentication has been successful
                        confirmed = !user.twoFactorEnabled
                    )
                )

            return ResponseHandler.generateResponse(
                UserCredentialsResponse(
                    userId = user.id,
                    apiKey = apiToken.token,
                    verified = apiToken.confirmed
                )
            )
        }

        private fun oauth2FA(request: TwoFactorRequest): Response {
            consumeRateLimit(request.apiKey)

            val token =
                tokenRepository.findByIdOrNull(request.apiKey)
                    ?: throw AuthorizedUserException()

            if (token.confirmed)
                return ResponseHandler.generateResponse(HttpStatus.OK)

            val user =
                userRepository.findByIdOrNull(token.owner)
                    ?: throw UnsupportedOperationException()

            if (user.twoFactorSecret == null)
                return ResponseHandler.generateResponse(HttpStatus.OK)

            consumeRateLimit(user.email)

            if (!TOTP.validate(user.twoFactorSecret, request.code) &&
                request.code != user.twoFactorRecoveryCode
            )
                throw InvalidTwoFactorCodeException()

            tokenRepository.save(token.copy(confirmed = true))

            return ResponseHandler.generateResponse(HttpStatus.OK)
        }

        @GetMapping("/passwordHint")
        fun requestPasswordHint(
            @RequestIP ip: String,
            @RequestParam("email") emailParam: String?
        ): Response {
            val email =
                emailParam?.lowercase()
                    ?: return ResponseError.INVALID_BODY.toResponse()

            consumeRateLimit(ip)
            consumeRateLimit(email)

            consumeRateLimit(ip, rateLimitEmail)
            consumeRateLimit(email, rateLimitEmail)

            val user =
                userRepository.findByEmail(email)
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
            @RequestIP ip: String,
            @RequestParam("user") userID: String,
            @RequestParam("code") verificationCode: String
        ): Response {
            consumeRateLimit(ip)
            consumeRateLimit(userID)

            val user =
                userRepository.findById(UUID.fromString(userID)).orElse(null)
                    ?: return ResponseError.INVALID_BODY.toResponse()

            // check if user email is already verified
            if (user.emailVerified)
                return ResponseHandler.redirectResponse("$webUrl/verification/email")

            // check if the code is valid
            if (user.emailVerificationCode.toString() != verificationCode)
                return ResponseError.INVALID_BODY.toResponse()

            // check if the code is expired
            if (user.emailVerificationCodeExpiresAt?.before(Date()) == true)
                return ResponseError.INVALID_BODY.toResponse()

            // set email as verified
            userRepository.save(
                user.copy(
                    emailVerified = true,
                    emailVerificationCode = null
                )
            )

            return ResponseHandler.redirectResponse("$webUrl/verification/email")
        }

        private fun consumeRateLimit(
            key: String,
            rateLimitConfig: BaseRateLimitConfig = rateLimit
        ) {
            if (!rateLimitEnabled) return

            if (!rateLimitConfig.resolveBucket(key).tryConsume(1))
                throw RateLimitException()
        }
    }
