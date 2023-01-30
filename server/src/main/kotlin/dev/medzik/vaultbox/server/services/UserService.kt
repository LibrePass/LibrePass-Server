package dev.medzik.vaultbox.server.services

import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.vaultbox.server.components.AuthComponent
import dev.medzik.vaultbox.server.components.TokenType
import dev.medzik.vaultbox.server.database.UserRepository
import dev.medzik.vaultbox.server.database.UserTable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

class UserCredentials(val accessToken: String, val refreshToken: String)

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

    fun register(email: String, password: String, passwordHint: String?, encryptionKey: String) {
        val salt = generateSalt()
        val passwordHash = hashPassword(password, salt)

        val user = UserTable()
        user.email = email
        user.password = passwordHash
        user.passwordSalt = salt
        user.passwordHint = passwordHint
        user.encryptionKey = encryptionKey

        createUser(user)
    }

    fun login(email: String, password: String): UserCredentials? {
        val user = userRepository.findByEmail(email) ?: return null
        val passwordHash = hashPassword(password, user.passwordSalt)

        if (passwordHash != user.password) {
            return null
        }

        return UserCredentials(
            authComponent.generateToken(TokenType.ACCESS_TOKEN, user.id),
            authComponent.generateToken(TokenType.REFRESH_TOKEN, user.id)
        )
    }

    fun generateSalt(): ByteArray {
        return Salt.generate(saltLength)
    }

    fun hashPassword(password: String, salt: ByteArray): String {
        return Pbkdf2(passwordIterations).sha256(password, salt)
    }
}
