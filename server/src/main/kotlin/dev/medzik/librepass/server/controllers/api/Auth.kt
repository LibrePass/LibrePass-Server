package dev.medzik.librepass.server.controllers.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dev.medzik.libcrypto.X25519
import dev.medzik.librepass.server.components.annotations.RequestIP
import dev.medzik.librepass.server.controllers.advice.ServerException
import dev.medzik.librepass.server.database.TokenRepository
import dev.medzik.librepass.server.database.TokenTable
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.ratelimit.AuthControllerEmailRateLimitConfig
import dev.medzik.librepass.server.ratelimit.AuthControllerRateLimitConfig
import dev.medzik.librepass.server.ratelimit.BaseRateLimitConfig
import dev.medzik.librepass.server.services.EmailService
import dev.medzik.librepass.server.utils.CustomDispatchers
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.server.utils.Validator.validateSharedKey
import dev.medzik.librepass.types.api.*
import dev.medzik.librepass.utils.toHex
import dev.medzik.otp.OTPParameters
import dev.medzik.otp.OTPType
import dev.medzik.otp.TOTPGenerator
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.mail.MailException
import org.springframework.web.bind.annotation.*
import java.util.*

// The server's X25519 key pair is used for authentication by doing a "handshake".
//
// It is not required that the key pair be the same all the time, so it
// is generated when the server is started and each time it is restarted,
// the key is different.
val ServerPrivateKey: ByteArray = X25519.generatePrivateKey()
val ServerPublicKey: ByteArray = X25519.publicFromPrivate(ServerPrivateKey)

@RestController
@RequestMapping("/api/auth")
class AuthController @Autowired constructor(
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
    private val coroutineScope = CoroutineScope(CustomDispatchers.LOOM)

    @PostMapping("/register")
    fun register(
        @RequestIP ip: String,
        @Valid @RequestBody request: RegisterRequest
    ): Response {
        consumeRateLimit(ip)

        if (!validateSharedKey(request.publicKey, request.sharedKey))
            throw ServerException.InvalidSharedKey()

        val tomorrow = Date.from(Calendar.getInstance().apply { add(Calendar.HOUR, 24) }.toInstant())
        val user = userRepository.save(
            UserTable(
                email = request.email.lowercase(),
                passwordHint = request.passwordHint,
                // Argon2 parameters
                parallelism = request.parallelism,
                memory = request.memory,
                iterations = request.iterations,
                // X25519 key pair
                publicKey = request.publicKey,
                // Email verification code
                emailVerificationCode = UUID.randomUUID().toString(),
                emailVerificationCodeExpiresAt = tomorrow
            )
        )

        coroutineScope.launch {
            try {
                emailService.sendEmailVerification(
                    to = request.email,
                    userId = user.id.toString(),
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
        @Valid @Email @RequestParam("email") email: String
    ): Response {
        fun preLoginDefaultResponse(): Response {
            return ResponseHandler.generateResponse(
                PreLoginResponse(
                    // Default Argon2 parameters
                    parallelism = 3,
                    memory = 65536,
                    iterations = 4,
                    // Server X5519 public key
                    serverPublicKey = ServerPublicKey.toHex()
                )
            )
        }

        consumeRateLimit(ip)

        if (email.isEmpty())
            return preLoginDefaultResponse()

        val user = userRepository.findByEmail(email.lowercase())
            ?: return preLoginDefaultResponse()

        return ResponseHandler.generateResponse(
            PreLoginResponse(
                parallelism = user.parallelism,
                memory = user.memory,
                iterations = user.iterations,
                // Server X25519 public key
                serverPublicKey = ServerPublicKey.toHex()
            )
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
                    throw ServerException.InvalidBody("json syntax error")
                }
            }
            "2fa" -> {
                try {
                    return oauth2FA(
                        ip = ip,
                        request = Gson().fromJson(request, TwoFactorRequest::class.java)
                    )
                } catch (e: JsonSyntaxException) {
                    throw ServerException.InvalidBody("json syntax error")
                }
            }
        }

        throw ServerException.InvalidBody("invalid grant_type")
    }

    private fun oauthLogin(
        ip: String,
        request: LoginRequest
    ): Response {
        val emailAddress = request.email.lowercase()

        consumeRateLimit(emailAddress)

        val user = userRepository.findByEmail(emailAddress) ?: throw ServerException.UserNotFound()

        if (emailVerificationRequired && !user.emailVerified)
            throw ServerException.EmailNotVerified()

        if (!validateSharedKey(user, request.sharedKey))
            throw ServerException.InvalidSharedKey()

        val apiToken = tokenRepository.save(
            TokenTable(
                owner = user.id,
                lastIp = ip,
                // Account with enabled two-factor authentication must verify the code using grant_type=2fa
                confirmed = !user.twoFactorEnabled
            )
        )

        if (!user.twoFactorEnabled) {
            emailService.sendNewLogin(
                to = user.email,
                ip = ip
            )
        }

        return ResponseHandler.generateResponse(
            UserCredentialsResponse(
                userId = user.id,
                apiKey = apiToken.token,
                verified = apiToken.confirmed
            )
        )
    }

    private fun oauth2FA(
        ip: String,
        request: TwoFactorRequest
    ): Response {
        consumeRateLimit(request.apiKey)

        val token = tokenRepository.findByIdOrNull(request.apiKey) ?: throw ServerException.InvalidToken()

        if (token.confirmed)
            return ResponseHandler.generateResponse(HttpStatus.OK)

        val user = userRepository.findByIdOrNull(token.owner) ?: throw IllegalStateException()

        if (user.twoFactorSecret == null)
            return ResponseHandler.generateResponse(HttpStatus.OK)

        consumeRateLimit(user.email)

        val totpParameters = OTPParameters.builder()
            .type(OTPType.TOTP)
            .secret(OTPParameters.Secret(user.twoFactorSecret))
            .build()

        if (!TOTPGenerator.verify(totpParameters, request.code) &&
            request.code != user.twoFactorRecoveryCode
        ) throw ServerException.InvalidTwoFactor()

        coroutineScope.launch {
            emailService.sendNewLogin(
                to = user.email,
                ip = ip
            )
        }

        tokenRepository.save(token.copy(confirmed = true))

        return ResponseHandler.generateResponse(HttpStatus.OK)
    }

    @GetMapping("/passwordHint")
    fun requestPasswordHint(
        @RequestIP ip: String,
        @Valid @NotBlank @Email @RequestParam("email") emailParam: String
    ): Response {
        val email = emailParam.lowercase()

        consumeRateLimit(ip)
        consumeRateLimit(email)

        consumeRateLimit(ip, rateLimitEmail)
        consumeRateLimit(email, rateLimitEmail)

        val user = userRepository.findByEmail(email) ?: throw ServerException.UserNotFound()

        try {
            emailService.sendPasswordHint(
                to = user.email,
                hint = user.passwordHint
            )
        } catch (e: MailException) {
            logger.error("Failed to send password hint", e)

            throw ServerException.Mail("failed to send email with password hint")
        }

        return ResponseHandler.generateResponse(HttpStatus.OK)
    }

    @GetMapping("/verifyEmail")
    fun verifyEmail(
        @RequestIP ip: String,
        @RequestParam("user") userId: UUID,
        @Valid @NotBlank @RequestParam("code") verificationCode: String
    ): Response {
        consumeRateLimit(ip)
        consumeRateLimit(userId.toString())

        val user = userRepository.findById(userId).orElse(null) ?: throw ServerException.UserNotFound()

        // check if user email is already verified
        if (user.emailVerified)
            return ResponseHandler.redirectResponse("$webUrl/verification/email")

        // check if the code is valid
        if (user.emailVerificationCode.toString() != verificationCode)
            throw ServerException.EmailInvalidCode()

        // check if the code is expired
        if (user.emailVerificationCodeExpiresAt?.before(Date()) == true)
            throw ServerException.EmailInvalidCode()

        // set email as verified
        userRepository.save(
            user.copy(
                emailVerified = true,
                emailVerificationCode = null,
                emailVerificationCodeExpiresAt = null
            )
        )

        return ResponseHandler.redirectResponse("$webUrl/verification/email")
    }

    @GetMapping("/resendVerificationEmail")
    fun resendVerificationEmail(
        @RequestIP ip: String,
        @Valid @NotBlank @Email @RequestParam("email") emailParam: String
    ): Response {
        val email = emailParam.lowercase()

        consumeRateLimit(ip)
        consumeRateLimit(email)

        consumeRateLimit(ip, rateLimitEmail)
        consumeRateLimit(email, rateLimitEmail)

        val user = userRepository.findByEmail(email) ?: throw ServerException.UserNotFound()

        // check if user email is already verified
        if (user.emailVerified)
            throw ServerException.InvalidBody("email is already verified")

        userRepository.save(
            user.copy(
                emailVerificationCodeExpiresAt = Date()
            )
        )

        try {
            emailService.sendEmailVerification(
                to = email,
                userId = user.id.toString(),
                code = user.emailVerificationCode.toString()
            )
        } catch (e: MailException) {
            logger.error("Failed to resend verification email", e)

            throw ServerException.Mail("failed to resend verification email")
        }

        return ResponseHandler.generateResponse(HttpStatus.OK)
    }

    private fun consumeRateLimit(
        key: String,
        rateLimitConfig: BaseRateLimitConfig = rateLimit
    ) {
        if (!rateLimitEnabled) return

        if (!rateLimitConfig.resolveBucket(key).tryConsume(1))
            throw ServerException.RateLimit()
    }
}
