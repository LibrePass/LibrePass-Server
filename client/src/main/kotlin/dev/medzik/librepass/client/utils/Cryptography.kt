package dev.medzik.librepass.client.utils

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters

object Cryptography {
    /**
     * Number of iterations for the encryption key and final password hash.
     */
    const val EncryptionKeyIterations = 500

    /**
     * Default argon2id settings.
     */
    val DefaultArgon2idParameters = UserArgon2idParameters(
        parallelism = 3,
        memory = 65536, // 64MB
        iterations = 4,
        version = 19
    )

    /**
     * Compute base password hash
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
     * Compute final password hash
     * @param basePassword base password hash of the user
     * @param email email of the user
     */
    fun computeFinalPasswordHash(
        basePassword: String,
        email: String
    ): String = Pbkdf2(EncryptionKeyIterations)
        .sha256(basePassword, email.encodeToByteArray())

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
        val finalPasswordHash = computeFinalPasswordHash(basePasswordHash.toHexHash(), email)

        return PasswordHashes(
            basePasswordHash = basePasswordHash,
            basePasswordHashString = basePasswordHash.toHexHash(),
            finalPasswordHash = finalPasswordHash
        )
    }

    /**
     * Password hashes of the user.
     */
    class PasswordHashes(
        /**
         * Base password hash. Used for encrypting/decryption encryption key.
         */
        val basePasswordHash: Argon2Hash,
        /**
         * The string representation of [basePasswordHash].
         */
        val basePasswordHashString: String,
        /**
         * Final password hash. Used for authentication. Stored in the database.
         */
        val finalPasswordHash: String
    )
}
