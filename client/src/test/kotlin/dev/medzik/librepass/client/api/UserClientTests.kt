package dev.medzik.librepass.client.api

import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import dev.medzik.librepass.utils.fromHex
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class UserClientTests {
    companion object {
        private const val EMAIL = "test_user@example.com"
        private const val PASSWORD = "test"

        @BeforeAll
        @JvmStatic
        fun setup() {
            val authClient = AuthClient(API_URL)
            authClient.register(EMAIL, PASSWORD)
            // wait for 1 second to prevent unauthorized error
            Thread.sleep(1000)
        }

        @AfterAll
        @JvmStatic
        fun delete() {
            val authClient = AuthClient(API_URL)
            val credentials = authClient.login(EMAIL, PASSWORD)
            UserClient(EMAIL, credentials.apiKey, API_URL).deleteAccount(PASSWORD)
        }
    }

    private lateinit var userId: UUID
    private lateinit var aesKey: ByteArray

    private lateinit var userClient: UserClient
    private lateinit var cipherClient: CipherClient

    @BeforeEach
    fun beforeEach() {
        val authClient = AuthClient(API_URL)
        val credentials = authClient.login(EMAIL, PASSWORD)

        userId = credentials.userId
        aesKey = credentials.aesKey.fromHex()

        userClient = UserClient(EMAIL, credentials.apiKey, API_URL)
        cipherClient = CipherClient(credentials.apiKey, API_URL)
    }

    @Test
    fun changeEmail() {
        val testCipher =
            Cipher(
                id = UUID.randomUUID(),
                owner = userId,
                type = CipherType.Login,
                loginData =
                    CipherLoginData(
                        name = "test",
                        username = "test",
                        password = "test"
                    )
            )

        fun insertTestCipher() {
            cipherClient.save(EncryptedCipher(testCipher, aesKey))
        }

        fun checkCipher(aesKey: ByteArray) {
            val ciphers = cipherClient.getAll()

            val cipher = Cipher(ciphers[0], aesKey)

            assertEquals(testCipher.loginData, cipher.loginData)
        }

        insertTestCipher()

        checkCipher(aesKey)

        val newEmail = "newemail@example.com"

        userClient.changeEmail(newEmail, PASSWORD)

        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)

        // login with new email
        var authClient = AuthClient(API_URL)
        var credentials = authClient.login(newEmail, PASSWORD)
        userClient = UserClient(newEmail, credentials.apiKey, API_URL)
        cipherClient = CipherClient(credentials.apiKey, API_URL)
        aesKey = credentials.aesKey.fromHex()

        checkCipher(aesKey)

        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)

        // change email back
        userClient.changeEmail(EMAIL, PASSWORD)

        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)

        authClient = AuthClient(API_URL)
        credentials = authClient.login(EMAIL, PASSWORD)
        userClient = UserClient(EMAIL, credentials.apiKey, API_URL)
        cipherClient = CipherClient(credentials.apiKey, API_URL)
        aesKey = credentials.aesKey.fromHex()

        checkCipher(aesKey)
    }

    @Test
    fun changePassword() {
        val testCipher =
            Cipher(
                id = UUID.randomUUID(),
                owner = userId,
                type = CipherType.Login,
                loginData =
                    CipherLoginData(
                        name = "test",
                        username = "test",
                        password = "test"
                    )
            )

        fun insertTestCipher() {
            cipherClient.save(EncryptedCipher(testCipher, aesKey))
        }

        fun checkCipher(aesKey: ByteArray) {
            val ciphers = cipherClient.getAll()

            val cipher = Cipher(ciphers[0], aesKey)

            assertEquals(testCipher.loginData, cipher.loginData)
        }

        insertTestCipher()

        checkCipher(aesKey)

        userClient.changePassword(PASSWORD, "test2")

        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)

        // login with new password
        var authClient = AuthClient(API_URL)
        var credentials = authClient.login(EMAIL, "test2")
        userClient = UserClient(EMAIL, credentials.apiKey, API_URL)
        cipherClient = CipherClient(credentials.apiKey, API_URL)
        aesKey = credentials.aesKey.fromHex()

        checkCipher(aesKey)

        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)

        // change password back
        userClient.changePassword("test2", PASSWORD)

        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)

        authClient = AuthClient(API_URL)
        credentials = authClient.login(EMAIL, PASSWORD)
        userClient = UserClient(EMAIL, credentials.apiKey, API_URL)
        cipherClient = CipherClient(credentials.apiKey, API_URL)
        aesKey = credentials.aesKey.fromHex()

        checkCipher(aesKey)
    }
}
