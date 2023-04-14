package dev.medzik.librepass.types.api.auth

import java.util.*

/**
 * RegisterRequest is a representation of a request to register endpoint.
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val passwordHint: String? = null,
    val encryptionKey: String
)

/**
 * LoginRequest is a representation of a request to login endpoint.
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * RefreshRequest is a representation of a request to refresh endpoint.
 */
data class RefreshRequest(val refreshToken: String)

/**
 * UserCredentials is a representation of a user credentials.
 * Server returns this object after successful login or refresh.
 */
data class UserCredentials(
    val userId: UUID,
    val accessToken: String,
    val refreshToken: String,
    val encryptionKey: String
)
