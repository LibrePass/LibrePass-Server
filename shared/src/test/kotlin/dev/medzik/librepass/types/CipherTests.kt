package dev.medzik.librepass.types

import com.google.gson.Gson
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import dev.medzik.librepass.types.cipher.data.CipherField
import dev.medzik.librepass.types.cipher.data.CipherFieldType
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import dev.medzik.librepass.types.cipher.data.PasswordHistory
import dev.medzik.librepass.utils.fromHex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class CipherTests {
    // example aes key
    private val aesKey = "1234567890123456789012345678901212345678901234567890123456789012".fromHex()

    // example cipher
    private val cipher =
        Cipher(
            id = UUID.randomUUID(),
            owner = UUID.randomUUID(),
            type = CipherType.Login,
            loginData =
                CipherLoginData(
                    name = "Example",
                    username = "librepass@example.com",
                    password = "SomeSecretPassword123!",
                    passwordHistory =
                        listOf(
                            PasswordHistory(
                                password = "very secret password",
                                // current date without milliseconds (because it is broken when comparing dates)
                                lastUsed = Date(System.currentTimeMillis() / 1000 * 1000)
                            )
                        ),
                    fields =
                        listOf(
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
        val encryptedCipher = EncryptedCipher(cipher, aesKey)

        assertEquals(cipher.id, encryptedCipher.id)
        assertEquals(cipher.owner, encryptedCipher.owner)
        assertEquals(cipher.type, CipherType.from(encryptedCipher.type))
        assert(encryptedCipher.protectedData.isNotEmpty())
        assertEquals(cipher.collection, encryptedCipher.collection)
        assertEquals(cipher.favorite, encryptedCipher.favorite)
        assertEquals(cipher.rePrompt, encryptedCipher.rePrompt)
    }

    @Test
    fun `encrypt and decrypt cipher`() {
        val encryptedCipher = EncryptedCipher(cipher, aesKey)
        val decryptedCipher = Cipher(encryptedCipher, aesKey)

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
        val encryptedCipher = EncryptedCipher(cipher, aesKey)
        val cipherJson = Gson().toJson(encryptedCipher)

        assert(cipherJson.isNotEmpty())
    }

    @Test
    fun `encrypt and decrypt cipher from json`() {
        val gson = Gson()
        val cipherJson = gson.toJson(EncryptedCipher(cipher, aesKey))
        val encryptedCipher = gson.fromJson(cipherJson, EncryptedCipher::class.java)
        val decryptedCipher = Cipher(encryptedCipher, aesKey)

        assertEquals(cipher.id, decryptedCipher.id)
        assertEquals(cipher.owner, decryptedCipher.owner)
        assertEquals(cipher.type, decryptedCipher.type)
        assertEquals(cipher.loginData, decryptedCipher.loginData)
        assertEquals(cipher.collection, decryptedCipher.collection)
        assertEquals(cipher.favorite, decryptedCipher.favorite)
        assertEquals(cipher.rePrompt, decryptedCipher.rePrompt)
    }
}
