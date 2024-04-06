package dev.medzik.librepass.client.api

import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import dev.medzik.librepass.types.cipher.data.CipherSecureNoteData
import dev.medzik.librepass.utils.fromHex
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
        private const val EMAIL = "_test_cipher@example.com"
        private const val PASSWORD = "test password"

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

    private lateinit var aesKey: ByteArray

    @BeforeEach
    fun beforeEach() {
        val authClient = AuthClient(API_URL)

        val credentials = authClient.login(EMAIL, PASSWORD)

        cipherClient = CipherClient(credentials.apiKey, API_URL)
        userId = credentials.userId
        aesKey = credentials.aesKey.fromHex()
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

        val response = cipherClient.save(cipher, aesKey)

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
        val firstResponse = cipherClient.sync(lastSync, emptyList(), emptyList())

        assert(firstResponse.ids.isNotEmpty())
        assert(firstResponse.ciphers.isEmpty())

        val testCipher = Cipher(
            id = UUID.randomUUID(),
            owner = userId,
            type = CipherType.Login,
            loginData = CipherLoginData(
                name = "test_cipher"
            )
        )
        val encryptedTestCipher = EncryptedCipher(testCipher, aesKey)

        val toUpdate = listOf(encryptedTestCipher)
        val toDelete = firstResponse.ids

        val secondResponse = cipherClient.sync(lastSync, toUpdate, toDelete)

        assertEquals(1, secondResponse.ids.size)
        assertEquals(1, secondResponse.ciphers.size)
        assertEquals(encryptedTestCipher, secondResponse.ciphers[0])

        // wait for 1 second to make sure that the last sync time is different
        Thread.sleep(1000)

        val thirdResponse = cipherClient.sync(Date(), emptyList(), emptyList())

        assertEquals(1, thirdResponse.ids.size)
        assertEquals(0, thirdResponse.ciphers.size)
    }

    @Test
    fun oldSyncCiphers() {
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

        val encryptedCipher = cipherClient.get(cipherId)
        val cipher = Cipher(encryptedCipher, aesKey)
        assertEquals(CipherType.Login, cipher.type)
        val beforeUpdate = cipherClient.get(cipherId)

        // wait 1 second to make the date different
        Thread.sleep(1000)

        val newCipher = cipher.copy(
            type = CipherType.SecureNote,
            loginData = null,
            secureNoteData = CipherSecureNoteData(
                title = "test_cipher",
                note = "test"
            )
        )
        cipherClient.save(newCipher.withUpdatedLastModified(), aesKey)

        val updatedCipher = cipherClient.get(cipherId)
        assertEquals(1, updatedCipher.type)
        assertEquals(beforeUpdate.created, updatedCipher.created)
        assertNotEquals(beforeUpdate.lastModified, updatedCipher.lastModified)
    }

    @Test
    fun deleteCipher() {
        insertCipher()
        cipherClient.delete(cipherId)
    }
}
