package dev.medzik.librepass.client.utils

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Curve25519
import dev.medzik.libcrypto.Pbkdf2
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
     * Calculate secret key from private and public keys. Used for AES encryption.
     */
    fun calculateSecretKey(privateKey: String, publicKey: String): String {
        return Curve25519.calculateAgreement(privateKey, publicKey)
    }

    /**
     * Compute base password hash.
     * @param password password of the user
     * @param email email of the user
     * @param parameters argon2id parameters
     */
    fun computeBasePasswordHash(
        password: String,
        email: String,
        parameters: UserArgon2idParameters = DefaultArgon2idParameters
    ): Argon2Hash {
        return parameters
            .toHashingFunction()
            .hash(password, email.toByteArray())
    }

    /**
     * Compute final password hash.
     * @param password password of the user (not hashed)
     * @param basePassword base password hash of the user (hashed)
     */
    fun computeFinalPasswordHash(
        password: String,
        basePassword: String
    ): String = Pbkdf2(1).sha256(basePassword, password.toByteArray())

    /**
     * Compute password hashes of the user.
     * @param password password of the user
     * @param email email of the user
     * @return password hashes
     */
    fun computeHashes(
        password: String,
        email: String,
    ): PasswordHashes {
        val basePasswordHash = computeBasePasswordHash(password, email)
        val finalPasswordHash = computeFinalPasswordHash(password, basePasswordHash.toHexHash())

        return PasswordHashes(
            basePasswordHash = basePasswordHash,
            finalPasswordHash = finalPasswordHash
        )
    }

    /**
     * User password hashes.
     * @property basePasswordHash base password hash, used for encrypt and decrypt the private key, not stored in the database
     * @property finalPasswordHash final password hash, used for authentication, stored in the database
     */
    class PasswordHashes(
        val basePasswordHash: Argon2Hash,
        val finalPasswordHash: String
    )
}
