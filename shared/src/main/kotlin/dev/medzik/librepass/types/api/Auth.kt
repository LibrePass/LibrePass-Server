package dev.medzik.librepass.types.api

import dev.medzik.libcrypto.Argon2
import java.util.*

/**
 * Request for register endpoint, for registration new users.
 *
 * @property email The user's email address.
 * @property passwordHint The hint for the user's password.
 * @property sharedKey The shared key with server, to verify the public key.
 * @property parallelism The argon2id parallelism parameter.
 * @property memory The argon2id memory parameter.
 * @property iterations The argon2id iterations parameter.
 * @property publicKey The X25519 public key of the user's password.
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
 * Response from preLogin endpoint, to obtain parameters for login endpoint.
 * Argon2id parameters for computing password hash and server's public key for "handshake".
 *
 * @property parallelism The argon2id parallelism parameter.
 * @property memory The argon2id memory parameter.
 * @property iterations The argon2id iterations parameter.
 * @property serverPublicKey The X25519 server's public key for handshaking.
 */
data class PreLoginResponse(
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val serverPublicKey: String
) {
    /** Creates argon2 hasher. */
    fun toArgon2(): Argon2 {
        return Argon2(
            32,
            parallelism,
            memory,
            iterations
        )
    }
}

/**
 * Request for oauth endpoint, for user authentication.
 *
 * @property email The user's email address.
 * @property sharedKey The shared key between the user and the server.
 */
data class LoginRequest(
    val email: String,
    val sharedKey: String
)

/**
 * Request for oauth endpoint, for verification 2-factor authentication.
 *
 * @property apiKey The user's API key returned by the login endpoint.
 * @property code The current 2-fa code.
 */
data class TwoFactorRequest(
    val apiKey: String,
    val code: String
)

/**
 * Response from oauth endpoint.
 *
 * @property userId The user's identifier.
 * @property apiKey The user's API key.
 * @property verified If false, you need to authenticate the API key with TOTP code to verify the login.
 * False always when the user has enabled 2-factor authentication.
 * To verify the user's API key use oauth with the 2-fa grant type.
 */
data class UserCredentialsResponse(
    val userId: UUID,
    val apiKey: String,
    val verified: Boolean
)
