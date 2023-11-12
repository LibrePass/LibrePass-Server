package dev.medzik.librepass.server.controllers.api

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.components.AuthorizedUser
import dev.medzik.librepass.server.controllers.advice.InvalidTwoFactorCodeException
import dev.medzik.librepass.server.database.CipherRepository
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.server.utils.Validator.validateSharedKey
import dev.medzik.librepass.server.utils.toResponse
import dev.medzik.librepass.types.api.ChangePasswordRequest
import dev.medzik.librepass.types.api.SetupTwoFactorRequest
import dev.medzik.librepass.types.api.SetupTwoFactorResponse
import dev.medzik.librepass.utils.TOTP
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
        private val cipherRepository: CipherRepository
    ) {
        @PatchMapping("/password")
        fun changePassword(
            @AuthorizedUser user: UserTable,
            @RequestBody body: ChangePasswordRequest
        ): Response {
            // validate shared key with an old public key
            if (!validateSharedKey(user, body.oldSharedKey))
                return ResponseError.INVALID_CREDENTIALS.toResponse()

            // validate shared key with a new public key
            if (!validateSharedKey(body.newPublicKey, body.newSharedKey))
                return ResponseError.INVALID_CREDENTIALS.toResponse()

            // get all user cipher ids
            val cipherIds = cipherRepository.getAllIds(user.id)

            // check if all ciphers are present
            // by the way checks if they are owned by the user (because `cipherIds` is a list of user cipher ids)
            body.ciphers.forEach { cipherData ->
                if (!cipherIds.contains(cipherData.id))
                    return ResponseError.INVALID_BODY.toResponse()
            }

            // update ciphers data due to re-encryption with new password
            body.ciphers.forEach { cipherData ->
                cipherRepository.updateData(
                    cipherData.id,
                    cipherData.data
                )
            }

            // update user in database
            userRepository.save(
                user.copy(
                    passwordHint = body.newPasswordHint,
                    // Argon2id parameters
                    parallelism = body.parallelism,
                    memory = body.memory,
                    iterations = body.iterations,
                    // Curve25519 public key
                    publicKey = body.newPublicKey,
                    // set the last password change date to now
                    lastPasswordChange = Date()
                )
            )

            return ResponseHandler.generateResponse(HttpStatus.OK)
        }

        @PostMapping("/setup/2fa")
        fun setupTwoFactor(
            @AuthorizedUser user: UserTable,
            @RequestBody body: SetupTwoFactorRequest
        ): Response {
            if (!validateSharedKey(user, body.sharedKey))
                return ResponseError.INVALID_CREDENTIALS.toResponse()

            if (body.code != TOTP.getTOTPCode(body.secret))
                throw InvalidTwoFactorCodeException()

            val recoveryCode = UUID.randomUUID().toString()

            userRepository.save(
                user.copy(
                    twoFactorEnabled = true,
                    twoFactorSecret = body.secret,
                    twoFactorRecoveryCode = recoveryCode
                )
            )

            val response = SetupTwoFactorResponse(recoveryCode = recoveryCode)
            return ResponseHandler.generateResponse(response, HttpStatus.OK)
        }
    }
