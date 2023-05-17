package dev.medzik.librepass.types.api.auth

import com.password4j.types.Argon2
import dev.medzik.libcrypto.Argon2HashingFunction
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
    val encryptionKey: String,

    // argon2id parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,

    // RSA keypair
     val publicKey: String,
     val privateKey: String
)

/**
 * UserArgon2idParameters is a representation of argon2 parameters.
 */
@Serializable
data class UserArgon2idParameters(
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int
) {
    fun toHashingFunction(): Argon2HashingFunction {
        return Argon2HashingFunction(
            256 / 8, // 256 bits
            parallelism,
            memory,
            iterations,
            Argon2.ID,
            version
        )
    }
}

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
    val encryptionKey: String
)
