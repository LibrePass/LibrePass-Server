package dev.medzik.librepass.client.api.v1

import dev.medzik.librepass.client.utils.Cryptography.DefaultArgon2idParameters
import net.datafaker.Faker
import org.junit.jupiter.api.Test

class AuthClientTests {
    private val authClient = AuthClient("http://localhost:8080")

    private val email = "_test_" + Faker().internet().emailAddress()
    private val password = Faker().internet().password()

    @Test
    fun register() {
        authClient.register(email, password)
        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)
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
        authClient.login(email, password)
    }
}
