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

data class PreLoginResponse(
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,
    val serverPublicKey: String
) {
    fun toArgon2(): Argon2 {
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

data class LoginRequest(
    val email: String,
    val sharedKey: String
)

// data class TwoFactorRequest(val code: Int)

// data class RefreshRequest(val refreshToken: String)

data class UserCredentialsResponse(
    val userId: UUID,
    val apiKey: String
)

// data class LoginResponse2FA(
//    val twoFactorRequired: Boolean,
//    val twoFactorToken: String? = null,
// )
