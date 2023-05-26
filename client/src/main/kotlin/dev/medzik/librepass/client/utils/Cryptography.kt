package dev.medzik.librepass.client.utils

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import org.apache.commons.codec.binary.Hex

/**
 * Cryptography utilities. Used for password hashing.
 */
object Cryptography {
    /**
     * RSA key size.
     */
    const val RSAKeySize = 4096

    /**
     * Number of iterations for the final password hash.
     */
    private const val FinalHashIterations = 500

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
     * Create random encryption key.
     * @return encryption key
     */
    fun createEncryptionKey(): String {
        val key = Hex.encodeHexString(Salt.generate(16))
        val salt = Salt.generate(16)

        return Pbkdf2(1).sha256(key, salt)
    }

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
    ): String = Pbkdf2(FinalHashIterations).sha256(basePassword, email.encodeToByteArray())

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
