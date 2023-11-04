package dev.medzik.librepass.client.api

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class AuthClientTests {
    companion object {
        private val authClient = AuthClient(API_URL)

        private const val EMAIL = "_test_user@example.com"
        private const val PASSWORD = "_test_user@example.com"

        @BeforeAll
        @JvmStatic
        fun register() {
            authClient.register(EMAIL, PASSWORD)
            // wait for 1 second to prevent unauthorized error
            Thread.sleep(1000)
        }
    }

    @Test
    fun login() {
        authClient.login(EMAIL, PASSWORD)
    }

    @Test
    fun `request password hint`() {
        authClient.requestPasswordHint(EMAIL)
    }
}
