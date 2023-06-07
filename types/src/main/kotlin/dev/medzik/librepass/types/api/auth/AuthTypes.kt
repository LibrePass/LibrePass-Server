package dev.medzik.librepass.types.api.auth

import dev.medzik.libcrypto.AesCbc
import dev.medzik.libcrypto.Argon2
import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Argon2Type
import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val passwordHint: String? = null,
    val protectedEncryptionKey: String,

    // argon2id parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,

    // RSA keypair
    val publicKey: String,
    val privateKey: String
)

@Serializable
data class UserArgon2idParameters(
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int
) {
    /**
     * Convert to Argon2 instance.
     * @return [Argon2]
     */
    fun toHashingFunction(): Argon2 {
        return Argon2(
            256 / 8, // 256 bits
            parallelism,
            memory,
            iterations,
            Argon2Type.ID,
            version
        )
    }
}

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserCredentials(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val accessToken: String,
    val protectedEncryptionKey: String
) {
    /**
     * Decrypt encryption key using base password hash.
     */
    fun decryptEncryptionKey(basePasswordHash: Argon2Hash): String {
        return AesCbc.decrypt(basePasswordHash.toHexHash(), protectedEncryptionKey)
    }
}
