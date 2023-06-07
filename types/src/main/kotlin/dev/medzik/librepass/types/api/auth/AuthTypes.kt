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
    val passwordHash: String,
    val passwordHint: String? = null,

    // argon2id parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,

    // Curve25519 key pair
    val publicKey: String,
    val protectedPrivateKey: String
)

@Serializable
data class UserArgon2idParameters(
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int
) {
    /**
     * Convert to [Argon2] instance.
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
    val passwordHash: String
)

@Serializable
data class UserCredentials(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val apiKey: String,
    val publicKey: String,
    val protectedPrivateKey: String,
) {
    fun decryptPrivateKey(basePasswordHash: Argon2Hash): String {
        return AesCbc.decrypt(basePasswordHash.toHexHash(), protectedPrivateKey)
    }
}
