package dev.medzik.librepass.server.controllers.api

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.components.AuthorizedUser
import dev.medzik.librepass.server.controllers.advice.InvalidTwoFactorCodeException
import dev.medzik.librepass.server.database.*
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.server.utils.Validator.validateSharedKey
import dev.medzik.librepass.server.utils.toResponse
import dev.medzik.librepass.types.api.ChangePasswordRequest
import dev.medzik.librepass.types.api.DeleteAccountRequest
import dev.medzik.librepass.types.api.SetupTwoFactorRequest
import dev.medzik.librepass.types.api.SetupTwoFactorResponse
import dev.medzik.librepass.utils.TOTP
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
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
        private val collectionRepository: CollectionRepository
    ) {
        @PatchMapping("/password")
        fun changePassword(
            @AuthorizedUser user: UserTable,
            @Valid @RequestBody request: ChangePasswordRequest
        ): Response {
            // validate shared key with an old public key
            if (!validateSharedKey(user, request.oldSharedKey))
                return ResponseError.INVALID_CREDENTIALS.toResponse()

            // validate shared key with a new public key
            if (!validateSharedKey(request.newPublicKey, request.newSharedKey))
                return ResponseError.INVALID_CREDENTIALS.toResponse()

            // get all user cipher ids
            val cipherIds = cipherRepository.getAllIds(user.id)

            // check if all ciphers are present
            // by the way checks if they are owned by the user (because `cipherIds` is a list of user cipher ids)
            request.ciphers.forEach { cipherData ->
                if (!cipherIds.contains(cipherData.id))
                    return ResponseError.INVALID_BODY.toResponse()
            }

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

        @PostMapping("/setup/2fa")
        fun setupTwoFactor(
            @AuthorizedUser user: UserTable,
            @Valid @RequestBody request: SetupTwoFactorRequest
        ): Response {
            if (!validateSharedKey(user, request.sharedKey))
                return ResponseError.INVALID_CREDENTIALS.toResponse()

            if (request.code != TOTP.getTOTPCode(request.secret))
                throw InvalidTwoFactorCodeException()

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
                return ResponseError.INVALID_CREDENTIALS.toResponse()

            if (user.twoFactorEnabled && request.code != TOTP.getTOTPCode(user.twoFactorSecret!!))
                throw InvalidTwoFactorCodeException()

            collectionRepository.deleteAllByOwner(user.id)
            cipherRepository.deleteAllByOwner(user.id)
            tokenRepository.deleteAllByOwner(user.id)
            userRepository.delete(user)

            return ResponseHandler.generateResponse(HttpStatus.OK)
        }
    }
