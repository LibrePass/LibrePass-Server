package dev.medzik.librepass.types.api

import dev.medzik.libcrypto.Argon2
import java.util.*

/**
 * Request for register endpoint, used to register new users.
 *
 * @property email The email address of the user.
 * @property passwordHint The password hint for password (optional but recommended).
 * @property sharedKey The shared key with server, to verify the public key.
 * @property parallelism The number of threads to use for calculating argon2 hash.
 * @property memory The memory to use for calculating argon2 hash.
 * @property iterations The number of iterations for calculating argon2 hash.
 * @property publicKey The X25519 public key.
 */
data class RegisterRequest(
    val email: String,
    val passwordHint: String? = null,
    val sharedKey: String,
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val publicKey: String
)

/**
 * Response from preLogin endpoint, used to obtain parameters to calculate private key
 *
 * @property parallelism The number of threads to use for calculating argon2 hash.
 * @property memory The memory to use for calculating argon2 hash.
 * @property iterations The number of iterations for calculating argon2 hash.
 * @property serverPublicKey The server's public key used to calculate the shared key with the server.
 */
data class PreLoginResponse(
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val serverPublicKey: String
) {
    /** Creates argon2 hasher */
    fun toArgon2(): Argon2 {
        return Argon2(
            32, // 256 bits
            parallelism,
            memory,
            iterations
        )
    }
}

/**
 * Request for oauth endpoint, used to authenticate users.
 *
 * @property email The email address of the user.
 * @property sharedKey The shared key with server, used for authentication.
 */
data class LoginRequest(
    val email: String,
    val sharedKey: String
)

/**
 * Request for oauth endpoint, used to verify login using OTP code.
 *
 * @property apiKey The API key returned by the login endpoint.
 * @property code The OTP code.
 */
data class TwoFactorRequest(
    val apiKey: String,
    val code: String
)

/**
 * Response from oauth endpoint.
 *
 * @property userId The identifier of the user.
 * @property apiKey The API key.
 * @property verified If false, you need to authenticate the API key with OTP code to use the API. (Only if the user enabled
 * 2FA authentication)
 */
data class UserCredentialsResponse(
    val userId: UUID,
    val apiKey: String,
    val verified: Boolean
)
