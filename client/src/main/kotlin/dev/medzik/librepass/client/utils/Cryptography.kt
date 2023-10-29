package dev.medzik.librepass.client.utils

import dev.medzik.libcrypto.Argon2
import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.X25519

object Cryptography {
    /** Compute secret key for a user key pair. */
    @JvmStatic
    fun computeSecretKey(privateKey: ByteArray): ByteArray {
        return computeSharedKey(privateKey, X25519.publicFromPrivate(privateKey))
    }

    /**
     * Compute shared key from private and public keys.
     * Used for AES encryption.
     */
    @JvmStatic
    fun computeSharedKey(
        privateKey: ByteArray,
        publicKey: ByteArray
    ): ByteArray {
        return X25519.computeSharedSecret(privateKey, publicKey)
    }

    /** Compute password hash. */
    @JvmStatic
    fun computePasswordHash(
        password: String,
        email: String,
        argon2Function: Argon2
    ): Argon2Hash {
        return argon2Function
            .hash(password, email.toByteArray())
    }

    /** Compute secret key from password. */
    @JvmStatic
    fun computeSecretKeyFromPassword(
        email: String,
        password: String,
        argon2Function: Argon2
    ): ByteArray {
        val passwordHash = computePasswordHash(password, email, argon2Function)
        return computeSecretKey(passwordHash.hash)
    }
}
