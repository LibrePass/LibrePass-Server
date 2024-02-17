package dev.medzik.librepass.server.controllers.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.medzik.librepass.errors.*
import dev.medzik.librepass.server.components.AuthorizedUser
import dev.medzik.librepass.server.components.RequestIP
import dev.medzik.librepass.server.database.*
import dev.medzik.librepass.server.ratelimit.AuthControllerRateLimitConfig
import dev.medzik.librepass.server.ratelimit.BaseRateLimitConfig
import dev.medzik.librepass.server.services.EmailService
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.server.utils.Validator.validateSharedKey
import dev.medzik.librepass.types.api.*
import dev.medzik.librepass.utils.TOTP
import jakarta.validation.Valid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/user")
class UserController
    @Autowired
    constructor(
        private val userRepository: UserRepository,
        private val tokenRepository: TokenRepository,
        private val cipherRepository: CipherRepository,
        private val collectionRepository: CollectionRepository,
        private val emailChangeRepository: EmailChangeRepository,
        private val emailService: EmailService,
        @Value("\${server.api.rateLimit.enabled}")
        private val rateLimitEnabled: Boolean,
        @Value("\${web.url}")
        private val webUrl: String,
        @Value("\${email.verification.required}")
        private val emailVerificationRequired: Boolean,
    ) {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private val rateLimit = AuthControllerRateLimitConfig()
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        @PatchMapping("/email")
        fun changeEmail(
            @AuthorizedUser user: UserTable,
            @Valid @RequestBody request: ChangeEmailRequest
        ): Response {
            changeEmailPasswordValidator(
                user = user,
                oldSharedKey = request.oldSharedKey,
                newPublicKey = request.newPublicKey,
                newSharedKey = request.newSharedKey,
                ciphers = request.ciphers
            )

            val emailChangeTable =
                emailChangeRepository.save(
                    EmailChangeTable(
                        owner = user.id,
                        newEmail = request.newEmail,
                        code = UUID.randomUUID().toString(),
                        codeExpiresAt =
                            Date.from(
                                Calendar.getInstance().apply {
                                    add(Calendar.HOUR, 24)
                                }.toInstant()
                            ),
                        newCiphers = Gson().toJson(request.ciphers),
                        newPublicKey = request.newPublicKey
                    )
                )

            if (!emailVerificationRequired) {
                verifyNewEmail(
                    internalCall = true,
                    ip = "",
                    userID = emailChangeTable.owner.toString(),
                    verificationCode = emailChangeTable.code
                )
            } else {
                coroutineScope.launch {
                    try {
                        emailService.sendChangeEmailVerification(
                            oldEmail = user.email,
                            newEmail = request.newEmail,
                            userId = user.id.toString(),
                            code = emailChangeTable.code
                        )
                    } catch (e: Throwable) {
                        logger.error("Error sending email verification", e)
                    }
                }
            }

            return ResponseHandler.generateResponse(HttpStatus.OK)
        }

        @GetMapping("/verifyNewEmail")
        fun verifyNewEmail(
            internalCall: Boolean = false,
            @RequestIP ip: String,
            @RequestParam("user") userID: String,
            @RequestParam("code") verificationCode: String
        ): Response {
            if (internalCall) {
                consumeRateLimit(ip)
                consumeRateLimit(userID)
            }

            val changeEmailTable =
                emailChangeRepository.findById(UUID.fromString(userID)).orElse(null)
                    ?: throw UserNotFoundException()

            // check if the code is valid
            if (changeEmailTable.code != verificationCode)
                throw EmailInvalidCodeException()

            // check if the code is expired
            if (changeEmailTable.codeExpiresAt.before(Date()))
                throw EmailInvalidCodeException()

            val user = userRepository.findById(changeEmailTable.owner).get()

            userRepository.save(
                user.copy(
                    email = changeEmailTable.newEmail,
                    publicKey = changeEmailTable.newPublicKey
                )
            )

            val ciphers: List<ChangePasswordCipherData> =
                Gson().fromJson(
                    changeEmailTable.newCiphers,
                    object : TypeToken<List<ChangePasswordCipherData>>() {}.type
                )

            for (cipher in ciphers) {
                cipherRepository.updateData(cipher.id, cipher.data)
            }

            emailChangeRepository.delete(changeEmailTable)

            return ResponseHandler.redirectResponse("$webUrl/verification/email")
        }

        @PatchMapping("/password")
        fun changePassword(
            @AuthorizedUser user: UserTable,
            @Valid @RequestBody request: ChangePasswordRequest
        ): Response {
            changeEmailPasswordValidator(
                user = user,
                oldSharedKey = request.oldSharedKey,
                newPublicKey = request.newPublicKey,
                newSharedKey = request.newSharedKey,
                ciphers = request.ciphers
            )

            // update ciphers data due to re-encryption with new password
            request.ciphers.forEach { cipherData ->
                cipherRepository.updateData(
                    cipherData.id,
                    cipherData.data
                )
            }

            // update user in database
            userRepository.save(
                user.copy(
                    passwordHint = request.newPasswordHint,
                    // Argon2id parameters
                    parallelism = request.parallelism,
                    memory = request.memory,
                    iterations = request.iterations,
                    // X25519 public key
                    publicKey = request.newPublicKey,
                    // set the last password change date to now
                    lastPasswordChange = Date()
                )
            )

            return ResponseHandler.generateResponse(HttpStatus.OK)
        }

        private fun changeEmailPasswordValidator(
            user: UserTable,
            oldSharedKey: String,
            newPublicKey: String,
            newSharedKey: String,
            ciphers: List<ChangePasswordCipherData>
        ) {
            // validate shared key with an old public key
            if (!validateSharedKey(user, oldSharedKey))
                throw InvalidSharedKeyException()

            // validate shared key with a new public key
            if (!validateSharedKey(newPublicKey, newSharedKey))
                throw InvalidSharedKeyException()

            // get all user cipher ids
            val cipherIds = cipherRepository.getAllIds(user.id)

            // check if all ciphers are present
            // by the way checks if they are owned by the user
            // (because `cipherIds` is a list of user cipher ids)
            ciphers.forEach { cipherData ->
                if (!cipherIds.contains(cipherData.id))
                    throw MissingCipherException()
            }
        }

        @PostMapping("/setup/2fa")
        fun setupTwoFactor(
            @AuthorizedUser user: UserTable,
            @Valid @RequestBody request: SetupTwoFactorRequest
        ): Response {
            if (!validateSharedKey(user, request.sharedKey))
                throw InvalidSharedKeyException()

            if (request.code != TOTP.getTOTPCode(request.secret))
                throw InvalidTwoFactorException()

            val recoveryCode = UUID.randomUUID().toString()

            userRepository.save(
                user.copy(
                    twoFactorEnabled = true,
                    twoFactorSecret = request.secret,
                    twoFactorRecoveryCode = recoveryCode
                )
            )

            val response = SetupTwoFactorResponse(recoveryCode = recoveryCode)
            return ResponseHandler.generateResponse(response, HttpStatus.OK)
        }

        @DeleteMapping("/delete")
        fun deleteAccount(
            @AuthorizedUser user: UserTable,
            @Valid @RequestBody request: DeleteAccountRequest
        ): Response {
            if (!validateSharedKey(user, request.sharedKey))
                throw InvalidSharedKeyException()

            if (user.twoFactorEnabled && request.code != TOTP.getTOTPCode(user.twoFactorSecret!!))
                throw InvalidTwoFactorException()

            collectionRepository.deleteAllByOwner(user.id)
            cipherRepository.deleteAllByOwner(user.id)
            tokenRepository.deleteAllByOwner(user.id)
            userRepository.delete(user)

            return ResponseHandler.generateResponse(HttpStatus.OK)
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
