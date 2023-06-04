package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.libcrypto.Argon2HashingFunction
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.server.components.AuthorizedUser
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.utils.*
import dev.medzik.librepass.types.api.user.ChangePasswordRequest
import dev.medzik.librepass.types.api.user.UserSecretsResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * User controller. Handles user-related requests. Requires authentication.
 */
@RestController
@RequestMapping("/api/v1/user")
class UserController @Autowired constructor(
    private val userRepository: UserRepository
) {
    /**
     * Change user password.
     */
    @PatchMapping("/password")
    fun changePassword(
        @AuthorizedUser user: UserTable?,
        @RequestBody body: ChangePasswordRequest
    ): Response {
        if (user == null)
            return ResponseError.Unauthorized

        // compare old password with password hash in database
        // if they match, update password hash with new password hash
        if (!Argon2HashingFunction.verify(body.oldPassword, user.password))
            return ResponseError.InvalidBody

        // compute new password hash
        val passwordSalt = Salt.generate(32)
        val newPasswordHash = Argon2DefaultHasher.hash(body.newPassword, passwordSalt)

        // update user in database
        userRepository.save(
            user.copy(
                password = newPasswordHash.toString(),
                encryptionKey = body.newProtectedEncryptionKey,
                // argon2id parameters
                parallelism = body.parallelism,
                memory = body.memory,
                iterations = body.iterations,
                version = body.version,
                // set last password change date
                lastPasswordChange = Date()
            )
        )

        return ResponseSuccess.OK
    }

    /**
     * Get user secrets (encryption key, RSA keypair).
     */
    @GetMapping("/secrets")
    fun getSecrets(@AuthorizedUser user: UserTable?): Response {
        if (user == null)
            return ResponseError.Unauthorized

        val secrets = UserSecretsResponse(
            encryptionKey = user.encryptionKey,
            privateKey = user.privateKey,
            publicKey = user.publicKey
        )

        return ResponseHandler.generateResponse(secrets, HttpStatus.OK)
    }
}
