package dev.medzik.librepass.client.utils

import org.junit.jupiter.api.Test

class CryptographyTests {
    @Test
    fun `generate keypair from password`() {
        val email = "example@example.com"
        val password = "password"
        val parameters = Cryptography.DefaultArgon2idParameters

        val passwordHash = Cryptography.computePasswordHash(email, password, parameters)
        Cryptography.generateKeyPairFromPrivate(passwordHash)
        Cryptography.generateKeyPairFromPrivate(passwordHash.toHexHash())
    }
}
