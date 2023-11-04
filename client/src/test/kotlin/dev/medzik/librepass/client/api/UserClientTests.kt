package dev.medzik.librepass.client.api

import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import dev.medzik.librepass.utils.fromHexString
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
    }

    private lateinit var userId: UUID
    private lateinit var secretKey: ByteArray

    private lateinit var userClient: UserClient
    private lateinit var cipherClient: CipherClient

    @BeforeEach
    fun beforeEach() {
        val authClient = AuthClient(API_URL)
        val credentials = authClient.login(EMAIL, PASSWORD)

        userId = credentials.userId
        secretKey = credentials.secretKey.fromHexString()

        userClient = UserClient(EMAIL, credentials.apiKey, API_URL)
        cipherClient = CipherClient(credentials.apiKey, API_URL)
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
            cipherClient.insert(EncryptedCipher(testCipher, secretKey))
        }

        fun checkCipher(secretKey: ByteArray) {
            val ciphers = cipherClient.getAll()

            val cipher = Cipher(ciphers[0], secretKey)

            assertEquals(testCipher.loginData, cipher.loginData)
        }

        insertTestCipher()

        checkCipher(secretKey)

        userClient.changePassword(PASSWORD, "test2")

        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)

        // login with new password
        var authClient = AuthClient(API_URL)
        var credentials = authClient.login(EMAIL, "test2")
        userClient = UserClient(EMAIL, credentials.apiKey, API_URL)
        cipherClient = CipherClient(credentials.apiKey, API_URL)
        secretKey = credentials.secretKey.fromHexString()

        checkCipher(secretKey)

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
        secretKey = credentials.secretKey.fromHexString()

        checkCipher(secretKey)
    }
}
