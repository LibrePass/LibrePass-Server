package dev.medzik.librepass.client.api

import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class CipherClientTests {
    private lateinit var cipherClient: CipherClient
    private lateinit var userId: UUID

    companion object {
        private const val email = "_test_cipher@example.com"
        private const val password = "test password"

        @BeforeAll
        @JvmStatic
        fun setup() {
            val authClient = AuthClient("http://localhost:8080")
            authClient.register(email, password)
            // wait for 1 second to prevent unauthorized error
            Thread.sleep(1000)
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            val authClient = AuthClient("http://localhost:8080")
            val credentials = authClient.login(email, password)

            val cipherClient = CipherClient(credentials.apiKey, "http://localhost:8080")

            cipherClient.getAll().forEach {
                cipherClient.delete(it.id)
            }
        }
    }

    private lateinit var secretKey: String

    @BeforeEach
    fun beforeEach() {
        val authClient = AuthClient("http://localhost:8080")

        val credentials = authClient.login(email, password)

        cipherClient = CipherClient(credentials.apiKey, "http://localhost:8080")
        userId = credentials.userId
        secretKey = credentials.secretKey
    }

    private lateinit var cipherId: UUID

    @Test
    fun insertCipher() {
        val cipher = Cipher(
            id = UUID.randomUUID(),
            owner = userId,
            type = CipherType.Login,
            loginData = CipherLoginData(
                name = "test_cipher"
            )
        )

        val response = cipherClient.insert(cipher, secretKey)

        cipherId = response.id
    }

    @Test
    fun getCipher() {
        insertCipher() // insert cipher before getting it
        cipherClient.get(cipherId)
    }

    @Test
    fun getAllCiphers() {
        val response = cipherClient.getAll()
        assertNotEquals(0, response.size)
    }

    @Test
    fun syncCiphers() {
        insertCipher()

        // wait for 1 second to make sure that the last sync time is different
        Thread.sleep(1000)

        val lastSync = Date()
        val response = cipherClient.sync(lastSync)

        assert(response.ids.isNotEmpty())
        assert(response.ciphers.isEmpty())
    }

    @Test
    fun updateCipher() {
        insertCipher()

        val cipher = cipherClient.get(cipherId)
        assertEquals(0, cipher.type)

        val newCipher = cipher.copy(type = 1)
        cipherClient.update(newCipher)

        val updatedCipher = cipherClient.get(cipherId)
        assertEquals(1, updatedCipher.type)
    }

    @Test
    fun deleteCipher() {
        insertCipher()
        cipherClient.delete(cipherId)
    }
}
