package dev.medzik.librepass.types

import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import dev.medzik.librepass.types.cipher.data.CipherField
import dev.medzik.librepass.types.cipher.data.CipherFieldType
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class CipherTests {
    // example secret key
    private val secretKey = "1234567890123456789012345678901212345678901234567890123456789012"

    // example cipher
    private val cipher = Cipher(
        id = UUID.randomUUID(),
        owner = UUID.randomUUID(),
        type = CipherType.Login,
        loginData = CipherLoginData(
            name = "Example",
            username = "librepass@example.com",
            password = "SomeSecretPassword123!",
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
        val encryptedCipher = EncryptedCipher(cipher, secretKey)

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
        val encryptedCipher = EncryptedCipher(cipher, secretKey)
        val decryptedCipher = Cipher(encryptedCipher, secretKey)

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
        val encryptedCipher = EncryptedCipher(cipher, secretKey)
        val cipherJson = encryptedCipher.toJson()

        assert(cipherJson.isNotEmpty())
    }

    @Test
    fun `encrypt and decrypt cipher from json`() {
        val cipherJson = EncryptedCipher(cipher, secretKey).toJson()
        val encryptedCipher = EncryptedCipher.from(cipherJson)
        val decryptedCipher = Cipher(encryptedCipher, secretKey)

        assertEquals(cipher.id, decryptedCipher.id)
        assertEquals(cipher.owner, decryptedCipher.owner)
        assertEquals(cipher.type, decryptedCipher.type)
        assertEquals(cipher.loginData, decryptedCipher.loginData)
        assertEquals(cipher.collection, decryptedCipher.collection)
        assertEquals(cipher.favorite, decryptedCipher.favorite)
        assertEquals(cipher.rePrompt, decryptedCipher.rePrompt)
    }
}
