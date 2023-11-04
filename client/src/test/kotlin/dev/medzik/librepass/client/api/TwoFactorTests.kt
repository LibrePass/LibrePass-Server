package dev.medzik.librepass.client.api

import dev.medzik.librepass.utils.TOTP
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class TwoFactorTests {
    companion object {
        private val authClient = AuthClient(API_URL)

        private const val EMAIL = "_test_2fa@example.com"
        private const val PASSWORD = "_test_2fa@example.com"

        private val twoFactorSecret = TOTP.generateSecretKey()

        @BeforeAll
        @JvmStatic
        fun register() {
            authClient.register(EMAIL, PASSWORD)
            // wait for 1 second to prevent unauthorized error
            Thread.sleep(1000)

            // setup 2fa
            val auth = authClient.login(EMAIL, PASSWORD)
            val code = TOTP.getTOTPCode(twoFactorSecret)
            UserClient(EMAIL, auth.apiKey, API_URL).setupTwoFactor(PASSWORD, twoFactorSecret, code)
        }
    }

    @Test
    fun testTwoFactorLogin() {
        val auth = authClient.login(EMAIL, PASSWORD)

        assertEquals(false, auth.apiKeyVerified)

        Thread.sleep(30 * 1000)

        val code = TOTP.getTOTPCode(twoFactorSecret)

        Thread.sleep(5 * 1000)

        authClient.loginTwoFactor(auth.apiKey, code)
    }
}
