package dev.medzik.librepass.types.api.auth

import com.password4j.types.Argon2
import dev.medzik.libcrypto.Argon2HashingFunction
import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

/**
 * RegisterRequest is a representation of a request to register endpoint.
 * It contains all the data needed to create a new user.
 * @property email The email of the user.
 * @property password The password of the user. (hashed)
 * @property passwordHint The password hint of the user.
 * @property encryptionKey The encryption key of the user. (encrypted)
 * @property parallelism The parallelism of the argon2id hashing function.
 * @property memory The memory of the argon2id hashing function.
 * @property iterations The iterations of the argon2id hashing function.
 * @property version The version of the argon2id hashing function.
 * @property publicKey The public key of the RSA keypair.
 * @property privateKey The private key of the RSA keypair.
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
 * @property parallelism The parallelism of the argon2id hashing function.
 * @property memory The memory of the argon2id hashing function.
 * @property iterations The iterations of the argon2id hashing function.
 * @property version The version of the argon2id hashing function.
 */
@Serializable
data class UserArgon2idParameters(
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int
) {
    /**
     * toHashingFunction converts UserArgon2idParameters to Argon2HashingFunction.
     * @return [Argon2HashingFunction]
     */
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
 * @property email The email of the user.
 * @property password The password of the user. (hashed)
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * UserCredentials is a representation of a user credentials.
 * Server returns this object after successful login or refresh.
 * @property userId The id of the user.
 * @property accessToken The access token of the user.
 * @property encryptionKey The encryption key of the user. (encrypted)
 */
@Serializable
data class UserCredentials(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val accessToken: String,
    val encryptionKey: String
)

