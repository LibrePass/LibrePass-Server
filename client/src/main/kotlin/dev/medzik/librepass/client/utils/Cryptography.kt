package dev.medzik.librepass.client.utils

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import java.util.*

/**
 * Cryptography utilities. Used for password hashing.
 */
object Cryptography {
    const val RSAKeySize = 4096

    val DefaultArgon2idParameters = UserArgon2idParameters(
        parallelism = 3,
        memory = 65536, // 64MB
        iterations = 4,
        version = 19
    )

    /**
     * Generate encryption key.
     */
    fun createEncryptionKey(): String {
        val key = Base64.getEncoder().encodeToString(Salt.generate(16))
        val salt = Salt.generate(16)

        return Pbkdf2(1).sha256(key, salt)
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

    class PasswordHashes(
        /**
         * Base Password Hash, used to encrypt the encryption key. It is not stored in the database.
         */
        val basePasswordHash: Argon2Hash,
        /**
         * Final Password Hash, used to authenticate the user. It is stored in the database.
         */
        val finalPasswordHash: String
    )
}
