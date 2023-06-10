package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.utils.*
import org.springframework.beans.factory.annotation.Autowired
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
//    /**
//     * Change user password.
//     */
//    @PatchMapping("/password")
//    fun changePassword(
//        @AuthorizedUser user: UserTable?,
//        @RequestBody body: ChangePasswordRequest
//    ): Response {
//        if (user == null)
//            return ResponseError.Unauthorized
//
//        // compare old password with password hash in database
//        // if they match, update password hash with new password hash
//        if (!Argon2.verify(body.oldPassword, user.passwordHash))
//            return ResponseError.InvalidBody
//
//        // compute new password hash
//        val passwordSalt = Salt.generate(32)
//        val newPasswordHash = Argon2DefaultHasher.hash(body.newPassword, passwordSalt)
//
//        // update user in database
//        userRepository.save(
//            user.copy(
//                passwordHash = newPasswordHash.toString(),
//                // Argon2id parameters
//                parallelism = body.parallelism,
//                memory = body.memory,
//                iterations = body.iterations,
//                version = body.version,
//                // Curve25519 key pair
//                protectedPrivateKey = body.newProtectedPrivateKey,
//                // Set last password change date to now
//                lastPasswordChange = Date()
//            )
//        )
//
//        return ResponseSuccess.OK
//    }
}
