package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.types.api.Cipher
import dev.medzik.librepass.types.api.CipherData
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CipherClientTests {
    private val encryptionKey = Pbkdf2(1).sha256("encryptionKey", Salt.generate(32))

    private lateinit var cipherClient: CipherClient
    private lateinit var userId: UUID

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            val authClient = AuthClient("http://localhost:8080")
            authClient.register("test_cipher@example.com", "test")
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            val authClient = AuthClient("http://localhost:8080")
            val credentials = authClient.login("test_cipher@example.com", "test")

            val cipherClient = CipherClient(credentials.accessToken, "http://localhost:8080")

            cipherClient.getAll().forEach {
                cipherClient.delete(it)
            }
        }
    }

    @BeforeEach
    fun beforeEach() {
        val authClient = AuthClient("http://localhost:8080")
        val credentials = authClient.login("test_cipher@example.com", "test")

        cipherClient = CipherClient(credentials.accessToken, "http://localhost:8080")
        userId = credentials.userId
    }

    private lateinit var cipherId: UUID

    @Test
    fun `insert cipher`() {
        val cipherData = CipherData()
        cipherData.name = "test_cipher"

        val cipher = Cipher()
        cipher.id = UUID.randomUUID()
        cipher.owner = userId
        cipher.type = 0
        cipher.data = cipherData

        val response = cipherClient.insert(cipher, encryptionKey)

        cipherId = response.id
    }

    @Test
    fun `get cipher`() {
        `insert cipher`() // insert cipher before getting it
        cipherClient.get(cipherId)
    }

    @Test
    fun `get all ciphers`() {
        val response = cipherClient.getAll()
        assertNotEquals(0, response.size)
    }

    @Test
    fun `update cipher`() {
        `insert cipher`()
        val cipher = cipherClient.get(cipherId)

        assertEquals(0, cipher.type.toInt())

        cipher.type = 1

        cipherClient.update(cipher)

        val updatedCipher = cipherClient.get(cipherId)

        assertEquals(1, updatedCipher.type.toInt())
    }

    @Test
    fun `delete cipher`() {
        `insert cipher`()
        cipherClient.delete(cipherId)
    }
}
