package dev.medzik.librepass.client.utils

import dev.medzik.libcrypto.Argon2
import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Curve25519
import dev.medzik.libcrypto.Curve25519KeyPair

object Cryptography {
    /**
     * Compute secret key for a user key pair.
     */
    @JvmStatic
    fun computeSecretKey(keyPair: Curve25519KeyPair): String {
        return computeSharedKey(keyPair.privateKey, keyPair.publicKey)
    }

    /**
     * Compute shared key from private and public keys.
     * Used for AES encryption.
     */
    @JvmStatic
    fun computeSharedKey(privateKey: String, publicKey: String): String {
        return Curve25519.computeSharedSecret(privateKey, publicKey)
    }

    /**
     * Compute password hash.
     */
    @JvmStatic
    fun computePasswordHash(
        password: String,
        email: String,
        argon2Function: Argon2
    ): Argon2Hash {
        return argon2Function
            .hash(password, email.toByteArray())
    }

    /**
     * Compute secret key from password.
     */
    @JvmStatic
    fun computeSecretKeyFromPassword(email: String, password: String, argon2Function: Argon2): String {
        val passwordHash = computePasswordHash(password, email, argon2Function)
        return computeSecretKeyFromPassword(passwordHash)
    }

    /**
     * Compute secret key from password hash.
     */
    @JvmStatic
    fun computeSecretKeyFromPassword(passwordHash: Argon2Hash): String {
        val keyPair = generateKeyPairFromPrivate(passwordHash)
        return computeSecretKey(keyPair)
    }

    /**
     * Generate a key pair from private key.
     */
    @JvmStatic
    fun generateKeyPairFromPrivate(privateKey: String): Curve25519KeyPair {
        return Curve25519.fromPrivateKey(privateKey)
    }

    /**
     * Generate a key pair from private key.
     */
    @JvmStatic
    fun generateKeyPairFromPrivate(passwordHash: Argon2Hash): Curve25519KeyPair {
        return generateKeyPairFromPrivate(passwordHash.toHexHash())
    }
}
