package dev.medzik.librepass.client.api

import dev.medzik.otp.OTPParameters
import dev.medzik.otp.OTPType
import dev.medzik.otp.TOTPGenerator
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class TwoFactorTests {
    companion object {
        private val authClient = AuthClient(API_URL)

        private const val EMAIL = "_test_2fa@example.com"
        private const val PASSWORD = "_test_2fa@example.com"

        private val twoFactorSecret = OTPParameters.Secret.generate().encoded
        private val otpParameters =
            OTPParameters.builder()
                .type(OTPType.TOTP)
                .secret(OTPParameters.Secret(twoFactorSecret))
                .label(OTPParameters.Label(""))
                .build()

        @BeforeAll
        @JvmStatic
        fun register() {
            authClient.register(EMAIL, PASSWORD)
            // wait for 1 second to prevent unauthorized error
            Thread.sleep(1000)

            // setup 2fa
            val auth = authClient.login(EMAIL, PASSWORD)
            val code = TOTPGenerator.now(otpParameters)
            UserClient(EMAIL, auth.apiKey, API_URL).setupTwoFactor(PASSWORD, twoFactorSecret, code)
        }

        @AfterAll
        @JvmStatic
        fun delete() {
            val credentials = authClient.login(EMAIL, PASSWORD)

            val code = TOTPGenerator.now(otpParameters)
            authClient.loginTwoFactor(credentials.apiKey, code)

            UserClient(EMAIL, credentials.apiKey, API_URL).deleteAccount(PASSWORD, code)
        }
    }

    @Test
    fun testTwoFactorLogin() {
        val auth = authClient.login(EMAIL, PASSWORD)

        assertEquals(false, auth.apiKeyVerified)

        Thread.sleep(30 * 1000)

        val code = TOTPGenerator.now(otpParameters)

        Thread.sleep(5 * 1000)

        authClient.loginTwoFactor(auth.apiKey, code)
    }
}
