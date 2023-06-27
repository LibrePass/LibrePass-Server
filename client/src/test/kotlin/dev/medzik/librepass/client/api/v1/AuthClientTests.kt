package dev.medzik.librepass.client.api.v1

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
    fun login() {
        authClient.login(email, password)
    }

    @Test
    fun `request password hint`() {
        authClient.requestPasswordHint(email)
    }
}
