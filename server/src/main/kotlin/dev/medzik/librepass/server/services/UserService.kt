package dev.medzik.librepass.server.services

import dev.medzik.libcrypto.Argon2HashingFunction
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.server.components.AuthComponent
import dev.medzik.librepass.server.components.TokenType
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.utils.Argon2DefaultHasher
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import dev.medzik.librepass.types.api.auth.UserCredentials
import dev.medzik.librepass.types.api.user.ChangePasswordRequest
import dev.medzik.librepass.types.api.user.UserSecretsResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService {
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var authComponent: AuthComponent

    fun createUser(user: UserTable): UserTable =
        userRepository.save(user)

    fun register(request: RegisterRequest): UserTable {
        val passwordSalt = Salt.generate(32)
        val passwordHash = Argon2DefaultHasher.hash(request.password, passwordSalt).toString()

        val verificationToken = UUID.randomUUID().toString()

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

        createUser(user)

        return user
    }

    fun getArgon2Parameters(email: String): UserArgon2idParameters? {
        if (email.isEmpty()) return null

        val user = userRepository.findByEmail(email) ?: return null

        return UserArgon2idParameters(
            parallelism = user.parallelism,
            memory = user.memory,
            iterations = user.iterations,
            version = user.version
        )
    }

    fun login(email: String, password: String): UserCredentials? {
        if (email.isEmpty() || password.isEmpty()) return null

        val user = userRepository.findByEmail(email) ?: return null

        if (!Argon2HashingFunction.verify(password, user.password)) return null

        return UserCredentials(
            userId = user.id,
            accessToken = authComponent.generateToken(TokenType.ACCESS_TOKEN, user.id),
            encryptionKey = user.encryptionKey
        )
    }

    fun verifyEmail(userId: String, verificationToken: String): Boolean {
        // get user from database
        val user = userRepository.findById(UUID.fromString(userId)).orElse(null)
            ?: return false

        // check if token is valid
        if (user.emailVerificationCode != verificationToken) return false

        // check if token is expired
        if (user.emailVerificationCodeExpiresAt?.before(Date()) == true) return false

        // set email as verified
        userRepository.save(user.copy(emailVerified = true))

        return true
    }

    /**
     * Find user in database using access token and return it or null if token is invalid.
     * @param accessToken Access token
     * @return [UserTable] or null
     */
    fun getUserByToken(accessToken: String): UserTable? {
        val userId = authComponent.parseToken(TokenType.ACCESS_TOKEN, accessToken) ?: return null
        val userUuid = UUID.fromString(userId)

        return userRepository.findById(userUuid).orElse(null)
    }

    fun changePassword(user: UserTable, body: ChangePasswordRequest): Boolean {
        // compare old password with password hash in database
        // if they match, update password hash with new password hash
        if (!Argon2HashingFunction.verify(body.oldPassword, user.password)) {
            return false
        }

        // update password hash in database
        val passwordSalt = Salt.generate(32)
        val newPasswordHash = Argon2DefaultHasher.hash(body.newPassword, passwordSalt)

        val newUser = user.copy(
            password = newPasswordHash.toString(),
            encryptionKey = body.newEncryptionKey,
            // argon2id parameters
            parallelism = body.parallelism,
            memory = body.memory,
            iterations = body.iterations,
            version = body.version,
            // set last password change date
            lastPasswordChange = Date()
        )

        userRepository.save(newUser)

        return true
    }

    fun getSecrets(user: UserTable): UserSecretsResponse {
        return UserSecretsResponse(
            encryptionKey = user.encryptionKey,
            privateKey = user.privateKey,
            publicKey = user.publicKey
        )
    }
}
