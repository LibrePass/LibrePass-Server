package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.libcrypto.Curve25519
import dev.medzik.librepass.server.components.AuthorizedUser
import dev.medzik.librepass.server.database.CipherRepository
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.utils.*
import dev.medzik.librepass.types.api.user.ChangePasswordRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * User controller. Handles user-related requests. Requires authentication.
 */
@RestController
@RequestMapping("/api/v1/user")
class UserController @Autowired constructor(
    private val userRepository: UserRepository,
    private val cipherRepository: CipherRepository
) {
    @PatchMapping("/password")
    fun changePassword(
        @AuthorizedUser user: UserTable?,
        @RequestBody body: ChangePasswordRequest
    ): Response {
        if (user == null)
            return ResponseError.Unauthorized

        println("SERVER KEY: ${ServerKeyPair.publicKey}")

        // compute shared key with new public key
        val sharedKey = Curve25519.computeSharedSecret(ServerKeyPair.privateKey, body.newPublicKey)

        println("SHARED KEY S: $sharedKey")
        println("SHARED KEY R: ${body.sharedKey}")

        // validate shared key
        if (body.sharedKey != sharedKey)
            return ResponseError.InvalidCredentials

        // get all user cipher ids
        val cipherIds = cipherRepository.getAllIds(user.id)

        // check if all ciphers are present
        // by the way checks if they are owned by the user (because `cipherIds` is a list of user cipher ids)
        body.ciphers.forEach { cipherData ->
            if (!cipherIds.contains(cipherData.id))
                return ResponseError.InvalidBody
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
                version = body.version,
                // Curve25519 public key
                publicKey = body.newPublicKey,
                // set last password change date to now
                lastPasswordChange = Date()
            )
        )

        return ResponseSuccess.OK
    }
}
