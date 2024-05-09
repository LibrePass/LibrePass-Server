package dev.medzik.librepass.utils

import dev.medzik.libcrypto.Argon2
import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.X25519

object Cryptography {
    /** Compute password hash. */
    @JvmStatic
    fun computePasswordHash(
        password: String,
        email: String,
        argon2Function: Argon2
    ): Argon2Hash {
        return argon2Function.hash(password, email.toByteArray())
    }

    /** Compute an AES key from the private key. */
    @JvmStatic
    fun computeAesKey(privateKey: ByteArray): ByteArray {
        return X25519.computeSharedSecret(privateKey, X25519.publicFromPrivate(privateKey))
    }
}
