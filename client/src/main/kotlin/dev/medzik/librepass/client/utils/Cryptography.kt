package dev.medzik.librepass.client.utils

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Curve25519
import dev.medzik.libcrypto.Curve25519KeyPair
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters

/**
 * Cryptography utilities. Used for password hashing.
 */
object Cryptography {
    val DefaultArgon2idParameters = UserArgon2idParameters(
        parallelism = 3,
        memory = 65536, // 64MB
        iterations = 4,
        version = 19
    )

    /**
     * Compute secret key for user key pair.
     */
    fun computeSecretKey(keyPair: Curve25519KeyPair): String {
        return computeSharedKey(keyPair.privateKey, keyPair.publicKey)
    }

    /**
     * Compute secret key from private and public keys. Used for AES encryption.
     */
    fun computeSharedKey(privateKey: String, publicKey: String): String {
        return Curve25519.computeSharedSecret(privateKey, publicKey)
    }

    /**
     * Compute password hash.
     * @param password password of the user
     * @param email email of the user
     * @param parameters argon2id parameters
     */
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
    fun computeSecretKeyFromPassword(email: String, password: String, parameters: UserArgon2idParameters): String {
        // compute base password hash
        val passwordHash = computePasswordHash(password, email, parameters)

        return computeSecretKeyFromPassword(passwordHash)
    }

    /**
     * Compute secret key from password hash.
     * @param passwordHash password hash of the user
     * @return secret key
     */
    fun computeSecretKeyFromPassword(passwordHash: Argon2Hash): String {
        val keyPair = Curve25519.fromPrivateKey(passwordHash.toHexHash())
        return computeSecretKey(keyPair)
    }
}
