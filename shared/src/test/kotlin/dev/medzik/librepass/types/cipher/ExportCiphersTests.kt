package dev.medzik.librepass.types.cipher

import dev.medzik.librepass.types.cipher.data.CipherField
import dev.medzik.librepass.types.cipher.data.CipherFieldType
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import dev.medzik.librepass.types.cipher.data.PasswordHistory
import dev.medzik.librepass.utils.fromHex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class ExportCiphersTest {
    @Test
    fun `test exporting and importing`() {
        val cipher1 =
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
        val cipher2 =
            Cipher(
                id = UUID.randomUUID(),
                owner = UUID.randomUUID(),
                type = CipherType.Login,
                loginData =
                    CipherLoginData(
                        name = "Example 2",
                        username = "librepass2@example.com",
                        password = "SomeOtherSecretPassword123!",
                        passwordHistory =
                            listOf(
                                PasswordHistory(
                                    password = "extremely secret password",
                                    // current date without milliseconds (because it is broken when comparing dates)
                                    lastUsed = Date(System.currentTimeMillis() / 1000 * 1000)
                                )
                            )
                    ),
                collection = UUID.randomUUID(),
                favorite = true,
                rePrompt = true
            )
        val cipherList = listOf(cipher1, cipher2)
        val aesKey = "1234567890123456789012345678901212345678901234567890123456789012".fromHex()

        val json = ExportCiphers.export(cipherList, aesKey)
        val importedCiphers = ExportCiphers.import(json, aesKey)

        assertEquals(cipherList, importedCiphers)
    }
}
