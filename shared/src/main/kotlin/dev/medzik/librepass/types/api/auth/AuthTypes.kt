package dev.medzik.librepass.types.api.auth

import dev.medzik.libcrypto.Argon2
import dev.medzik.libcrypto.Argon2Type
import java.util.*

data class RegisterRequest(
    val email: String,
    val passwordHint: String? = null,

    val sharedKey: String,

    // Argon2id parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,

    // Curve25519 public key
    val publicKey: String
)

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

data class ServerPublicKey(
    val publicKey: String
)

data class LoginRequest(
    val email: String,
    val sharedKey: String
)

data class LoginResponse(
    val userId: UUID,
    val apiKey: String
)

data class UserCredentials(
    val userId: UUID,
    val apiKey: String,
    val publicKey: String,
    val privateKey: String,
    val secretKey: String
)
