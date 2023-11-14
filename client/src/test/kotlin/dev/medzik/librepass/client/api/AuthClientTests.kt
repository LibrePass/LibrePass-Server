package dev.medzik.librepass.client.api

import org.junit.jupiter.api.AfterAll
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

        @AfterAll
        @JvmStatic
        fun delete() {
            val credentials = authClient.login(EMAIL, PASSWORD)
            UserClient(EMAIL, credentials.apiKey, API_URL).deleteAccount(PASSWORD)
        }
    }

    @Test
    fun login() {
        authClient.login(EMAIL, PASSWORD)
    }

    @Test
    fun requestPasswordHint() {
        authClient.requestPasswordHint(EMAIL)
    }

    @Test
    fun resendVerificationEmail() {
        authClient.resendVerificationEmail(EMAIL)
    }
}
