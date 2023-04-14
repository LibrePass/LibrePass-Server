package dev.medzik.librepass.server.services

import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.server.components.AuthComponent
import dev.medzik.librepass.server.components.TokenType
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.types.api.auth.UserCredentials
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService {
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var authComponent: AuthComponent

    private final val passwordIterations = 100000
    private final val saltLength = 32

    fun createUser(user: UserTable) {
        userRepository.save(user)
    }

    fun register(email: String, password: String, passwordHint: String?, encryptionKey: String): String {
        val salt = generateSalt()
        val passwordHash = hashPassword(password, salt)

        val user = UserTable()
        user.email = email
        user.password = passwordHash
        user.passwordSalt = salt
        user.passwordHint = passwordHint
        user.encryptionKey = encryptionKey

        createUser(user)

        return authComponent.generateToken(TokenType.VERIFICATION_TOKEN, user.id)
    }

    fun login(email: String, password: String): UserCredentials? {
        if (email.isEmpty() || password.isEmpty()) return null

        val user = userRepository.findByEmail(email) ?: return null
        val passwordHash = hashPassword(password, user.passwordSalt)

        if (passwordHash != user.password) return null

        return UserCredentials(
            userId = user.id,
            accessToken = authComponent.generateToken(TokenType.ACCESS_TOKEN, user.id),
            refreshToken = authComponent.generateToken(TokenType.REFRESH_TOKEN, user.id),
            encryptionKey = user.encryptionKey
        )
    }

    fun refreshToken(refreshToken: String): UserCredentials? {
        val userId = authComponent.parseToken(TokenType.REFRESH_TOKEN, refreshToken) ?: return null
        val userUuid = UUID.fromString(userId)

        val user = userRepository.findById(userUuid).orElse(null) ?: return null

        return UserCredentials(
            userId = userUuid,
            accessToken = authComponent.generateToken(TokenType.ACCESS_TOKEN, userUuid),
            refreshToken = authComponent.generateToken(TokenType.REFRESH_TOKEN, userUuid),
            encryptionKey = user.encryptionKey,
        )
    }

    fun verifyEmail(verificationToken: String): Boolean {
        val userId = authComponent.parseToken(TokenType.VERIFICATION_TOKEN, verificationToken) ?: return false
        val userUuid = UUID.fromString(userId)

        val user = userRepository.findById(userUuid).orElse(null) ?: return false

        user.emailVerified = true
        userRepository.save(user)

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

    fun generateSalt(): ByteArray = Salt.generate(saltLength)

    fun hashPassword(password: String, salt: ByteArray): String = Pbkdf2(passwordIterations).sha256(password, salt)
}
