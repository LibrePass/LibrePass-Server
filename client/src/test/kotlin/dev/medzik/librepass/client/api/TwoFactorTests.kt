package dev.medzik.librepass.client.api

import dev.medzik.librepass.utils.TOTP
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class TwoFactorTests {
    companion object {
        private const val apiUrl = "http://localhost:8080"
        private val authClient = AuthClient(apiUrl)

        private const val email = "_test_2fa@example.com"
        private const val password = "_test_2fa@example.com"

        private val twoFactorSecret = TOTP.generateSecretKey()

        @BeforeAll
        @JvmStatic
        fun register() {
            authClient.register(email, password)
            // wait for 1 second to prevent unauthorized error
            Thread.sleep(1000)

            // setup 2fa
            val auth = authClient.login(email, password)
            val code = TOTP.getTOTPCode(twoFactorSecret)
            UserClient(email, auth.apiKey, apiUrl).setupTwoFactor(password, twoFactorSecret, code)
        }
    }

    @Test
    fun testTwoFactorLogin() {
        val auth = authClient.login(email, password)

        assertEquals(false, auth.apiKeyVerified)

        Thread.sleep(30 * 1000)

        val code = TOTP.getTOTPCode(twoFactorSecret)

        Thread.sleep(5 * 1000)

        authClient.loginTwoFactor(auth.apiKey, code)
    }
}
