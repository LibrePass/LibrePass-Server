package dev.medzik.librepass.server.services

import dev.medzik.libcrypto.Argon2HashingFunction
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.server.components.AuthComponent
import dev.medzik.librepass.server.components.TokenType
import dev.medzik.librepass.server.database.UserRepository
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
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

    // create argon2 password encoder with default parameters
    private final val argon2 = Argon2HashingFunction(32, 1, 15 * 1024, 1)

    fun createUser(user: UserTable): UserTable = userRepository.save(user)

    fun register(request: RegisterRequest): String {
        val passwordSalt = Salt.generate(32)
        val passwordHash = argon2.hash(request.password, passwordSalt).toString()

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
            privateKey = request.privateKey
        )

        createUser(user)

        return authComponent.generateToken(TokenType.VERIFICATION_TOKEN, user.id)
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
}
