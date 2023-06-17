package dev.medzik.librepass.client.utils

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Curve25519
import dev.medzik.libcrypto.Curve25519KeyPair
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters

/**
 * Cryptography utilities. Used for password hashing.
 */
object Cryptography {
    @JvmStatic
    val DefaultArgon2idParameters = UserArgon2idParameters(
        parallelism = 3,
        memory = 65536, // 64MB
        iterations = 4,
        version = 19
    )

    /**
     * Compute secret key for user key pair.
     */
    @JvmStatic
    fun computeSecretKey(keyPair: Curve25519KeyPair): String {
        return computeSharedKey(keyPair.privateKey, keyPair.publicKey)
    }

    /**
     * Compute secret key from private and public keys. Used for AES encryption.
     */
    @JvmStatic
    fun computeSharedKey(privateKey: String, publicKey: String): String {
        return Curve25519.computeSharedSecret(privateKey, publicKey)
    }

    /**
     * Compute password hash.
     * @param password password of the user
     * @param email email of the user
     * @param parameters argon2id parameters
     */
    @JvmStatic
    fun computePasswordHash(
        password: String,
        email: String,
        parameters: UserArgon2idParameters = DefaultArgon2idParameters
    ): Argon2Hash {
        return parameters
            .toHashingFunction()
            .hash(password, email.toByteArray())
    }

    /**
     * Compute secret key from password.
     * @param email email of the user
     * @param password password of the user
     * @return secret key
     */
    @JvmStatic
    fun computeSecretKeyFromPassword(email: String, password: String, parameters: UserArgon2idParameters): String {
        val passwordHash = computePasswordHash(password, email, parameters)
        return computeSecretKeyFromPassword(passwordHash)
    }

    /**
     * Compute secret key from password hash.
     * @param passwordHash password hash of the user
     * @return secret key
     */
    @JvmStatic
    fun computeSecretKeyFromPassword(passwordHash: Argon2Hash): String {
        val keyPair = generateKeyPairFromPrivate(passwordHash)
        return computeSecretKey(keyPair)
    }

    /**
     * Generate key pair from private key.
     * @param privateKey private key
     */
    @JvmStatic
    fun generateKeyPairFromPrivate(privateKey: String): Curve25519KeyPair {
        return Curve25519.fromPrivateKey(privateKey)
    }

    /**
     * Generate key pair from private key.
     * @param privateKey private key (password hash)
     */
    @JvmStatic
    fun generateKeyPairFromPrivate(privateKey: Argon2Hash): Curve25519KeyPair {
        return generateKeyPairFromPrivate(privateKey.toHexHash())
    }
}
