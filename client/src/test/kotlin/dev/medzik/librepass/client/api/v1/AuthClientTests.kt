package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.Pbkdf2
import net.datafaker.Faker
import org.junit.jupiter.api.Test

class AuthClientTests {
    private val authClient = AuthClient("http://localhost:8080")

    private val email = "_test_" + Faker().internet().emailAddress()
    private val password = Faker().internet().password()
    // TODO: use the real password hash (argon2)
    private val passwordHash = Pbkdf2(100).sha256(password, "salt".toByteArray())

    @Test
    fun register() {
        authClient.register(email, passwordHash)
    }

    @Test
    fun `get user argon2id parameters`() {
        register() // register user first
        val parameters = authClient.getUserArgon2idParameters(email)

        assert(parameters.parallelism == DefaultArgon2idParameters.parallelism)
        assert(parameters.memory == DefaultArgon2idParameters.memory)
        assert(parameters.iterations == DefaultArgon2idParameters.iterations)
        assert(parameters.version == DefaultArgon2idParameters.version)
    }

    @Test
    fun login() {
        register() // register user first
        authClient.login(email, passwordHash)
    }

    @Test
    fun `refresh token`() {
        register() // register user first
        val credentials = authClient.login(email, passwordHash)
        authClient.refresh(credentials.refreshToken)
    }
}
