package dev.medzik.librepass.client.api.v1

import dev.medzik.librepass.client.utils.Cryptography
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class UserClientTests {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            val authClient = AuthClient("http://localhost:8080")
            authClient.register("test_user@example.com", "test")
            // wait for 1 second to prevent unauthorized error
            Thread.sleep(1000)
        }
    }

    private lateinit var userId: UUID
    private lateinit var secretKey: String

    private lateinit var userClient: UserClient
    private lateinit var cipherClient: CipherClient

    @BeforeEach
    fun beforeEach() {
        val authClient = AuthClient("http://localhost:8080")
        val credentials = authClient.login("test_user@example.com", "test")

        userId = credentials.userId
        secretKey = Cryptography.computeSecretKeyFromPassword(
            "test_user@example.com",
            "test",
            Cryptography.DefaultArgon2idParameters
        )

        userClient = UserClient("test_user@example.com", credentials.apiKey, "http://localhost:8080")
        cipherClient = CipherClient(credentials.apiKey, "http://localhost:8080")
    }

    @Test
    fun changePassword() {
        val testCipher = Cipher(
            id = UUID.randomUUID(),
            owner = userId,
            type = CipherType.Login,
            loginData = CipherLoginData(
                name = "test",
                username = "test",
                password = "test"
            )
        )

        fun insertTestCipher() {
            cipherClient.insert(EncryptedCipher(testCipher, secretKey))
        }

        fun checkCipher(password: String) {
            val ciphers = cipherClient.getAll()

            secretKey = Cryptography.computeSecretKeyFromPassword(
                "test_user@example.com",
                password,
                Cryptography.DefaultArgon2idParameters
            )

            val cipher = Cipher(ciphers[0], secretKey)

            assertEquals(testCipher.loginData, cipher.loginData)
        }

        insertTestCipher()

        checkCipher("test")

        userClient.changePassword("test", "test2")

        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)

        // login with new password
        var authClient = AuthClient("http://localhost:8080")
        var credentials = authClient.login("test_user@example.com", "test2")
        userClient = UserClient("test_user@example.com", credentials.apiKey, "http://localhost:8080")
        cipherClient = CipherClient(credentials.apiKey, "http://localhost:8080")

        checkCipher("test2")

        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)

        // change password back
        userClient.changePassword("test2", "test")

        // wait for 1 second to prevent unauthorized error
        Thread.sleep(1000)

        authClient = AuthClient("http://localhost:8080")
        credentials = authClient.login("test_user@example.com", "test")
        userClient = UserClient("test_user@example.com", credentials.apiKey, "http://localhost:8080")
        cipherClient = CipherClient(credentials.apiKey, "http://localhost:8080")

        checkCipher("test")
    }
}
