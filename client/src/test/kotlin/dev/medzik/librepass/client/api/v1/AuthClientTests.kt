package dev.medzik.librepass.client.api.v1

import dev.medzik.librepass.client.utils.Cryptography.DefaultArgon2idParameters
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class AuthClientTests {
    companion object {
        private val authClient = AuthClient("http://localhost:8080")

        private const val email = "_test_user@example.com"
        private const val password = "_test_user@example.com"

        @BeforeAll
        @JvmStatic
        fun register() {
            authClient.register(email, password)
            // wait for 1 second to prevent unauthorized error
            Thread.sleep(1000)
        }
    }

    @Test
    fun `get user argon2id parameters`() {
        val parameters = authClient.getUserArgon2idParameters(email)

        assert(parameters.parallelism == DefaultArgon2idParameters.parallelism)
        assert(parameters.memory == DefaultArgon2idParameters.memory)
        assert(parameters.iterations == DefaultArgon2idParameters.iterations)
        assert(parameters.version == DefaultArgon2idParameters.version)
    }

    @Test
    fun login() {
        authClient.login(email, password)
    }

    @Test
    fun `request password hint`() {
        authClient.requestPasswordHint(email)
    }
}
