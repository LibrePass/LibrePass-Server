package dev.medzik.librepass.types.api.auth

import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

/**
 * RegisterRequest is a representation of a request to register endpoint.
 */
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val passwordHint: String? = null,
    val encryptionKey: String
)

/**
 * LoginRequest is a representation of a request to login endpoint.
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * RefreshRequest is a representation of a request to refresh endpoint.
 */
@Serializable
data class RefreshRequest(val refreshToken: String)

/**
 * UserCredentials is a representation of a user credentials.
 * Server returns this object after successful login or refresh.
 */
@Serializable
data class UserCredentials(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val accessToken: String,
    val refreshToken: String,
    val encryptionKey: String
)
