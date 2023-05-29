package dev.medzik.librepass.types

import dev.medzik.libcrypto.Salt
import org.apache.commons.codec.binary.Hex
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class CipherTests {
    // generate encryption key
    private val encryptionKey = Hex.encodeHexString(Salt.generate(32))

    // example cipher
    private val cipher = Cipher(
        id = UUID.randomUUID(),
        owner = UUID.randomUUID(),
        type = CipherType.Login,
        loginData = LoginCipherData(
            name = "Test Cipher",
            username = "test",
            fields = listOf(
                CipherField(
                    name = "test",
                    type = CipherFieldType.Text,
                    value = "test"
                )
            )
        ),
        collection = UUID.randomUUID(),
        favorite = true,
        rePrompt = true
    )

    @Test
    fun `encrypt cipher`() {
        val encryptedCipher = EncryptedCipher(cipher, encryptionKey)

        assertEquals(cipher.id, encryptedCipher.id)
        assertEquals(cipher.owner, encryptedCipher.owner)
        assertEquals(cipher.type, CipherType.from(encryptedCipher.type))
        assert(encryptedCipher.data.isNotEmpty())
        assertEquals(cipher.collection, encryptedCipher.collection)
        assertEquals(cipher.favorite, encryptedCipher.favorite)
        assertEquals(cipher.rePrompt, encryptedCipher.rePrompt)
    }

    @Test
    fun `encrypt and decrypt cipher`() {
        val encryptedCipher = EncryptedCipher(cipher, encryptionKey)
        val decryptedCipher = Cipher(encryptedCipher, encryptionKey)

        assertEquals(cipher.id, decryptedCipher.id)
        assertEquals(cipher.owner, decryptedCipher.owner)
        assertEquals(cipher.type, decryptedCipher.type)
        assertEquals(cipher.loginData, decryptedCipher.loginData)
        assertEquals(cipher.collection, decryptedCipher.collection)
        assertEquals(cipher.favorite, decryptedCipher.favorite)
        assertEquals(cipher.rePrompt, decryptedCipher.rePrompt)
    }

    @Test
    fun `encrypt cipher to json`() {
        val encryptedCipher = EncryptedCipher(cipher, encryptionKey)
        val cipherJson = encryptedCipher.toJson()

        assert(cipherJson.isNotEmpty())
    }

    @Test
    fun `encrypt and decrypt cipher from json`() {
        val cipherJson = EncryptedCipher(cipher, encryptionKey).toJson()
        val encryptedCipher = EncryptedCipher.from(cipherJson)
        val decryptedCipher = Cipher(encryptedCipher, encryptionKey)

        assertEquals(cipher.id, decryptedCipher.id)
        assertEquals(cipher.owner, decryptedCipher.owner)
        assertEquals(cipher.type, decryptedCipher.type)
        assertEquals(cipher.loginData, decryptedCipher.loginData)
        assertEquals(cipher.collection, decryptedCipher.collection)
        assertEquals(cipher.favorite, decryptedCipher.favorite)
        assertEquals(cipher.rePrompt, decryptedCipher.rePrompt)
    }
}
