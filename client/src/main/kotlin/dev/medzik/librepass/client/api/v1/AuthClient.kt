package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.AesCbc
import dev.medzik.libcrypto.Argon2id
import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RefreshRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserCredentials
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Hex

const val EncryptionKeyIterations = 500

val Argon2idHasher = Argon2id(32, 1, 47104, 1)

@Suppress("unused")
class AuthClient(apiUrl: String = Client.DefaultApiUrl) {
    private val apiEndpoint = "/api/v1/auth"

    private val client = Client(null, apiUrl)

    /**
     * Register a new user
     * @param email email of the user
     * @param password password of the user
     * @param passwordHint password hint of the user (optional)
     */
    @Throws(ClientException::class, ApiException::class)
    fun register(email: String, password: String, passwordHint: String? = null) {
        val basePassword = computeBasePasswordHash(password, email)
        // compute the final password, it is required since the earlier hash is used to encrypt the encryption key
        val finalPassword = computeFinalPasswordHash(basePassword, email)

        // create a random byte array with 16 bytes and encode it to hex string,
        val encryptionKeyBase = Pbkdf2(EncryptionKeyIterations).sha256(Hex.encodeHexString(Salt.generate(16)), Salt.generate(16))
        val encryptionKey = AesCbc.encrypt(encryptionKeyBase, basePassword)

        val request = RegisterRequest(
            email = email,
            password = finalPassword,
            passwordHint = passwordHint,
            encryptionKey = encryptionKey
        )

        client.post("$apiEndpoint/register", Json.encodeToString(RegisterRequest.serializer(), request))
    }

    /**
     * Login a user
     * @param email email of the user
     * @param password password of the user
     * @param passwordIsBaseHash if the password is already the base password hash (default false)
     * @return [UserCredentials]
     */
    @Throws(ClientException::class, ApiException::class)
    fun login(email: String, password: String, passwordIsBaseHash: Boolean = false): UserCredentials {
        val basePassword = if (passwordIsBaseHash) password else computeBasePasswordHash(password, email)
        val finalPassword = computeFinalPasswordHash(basePassword, email)

        val request = LoginRequest(
            email = email,
            password = finalPassword
        )

        val body = client.post("$apiEndpoint/login", Json.encodeToString(LoginRequest.serializer(), request))

        return Json.decodeFromString(UserCredentials.serializer(), body)
    }

    /**
     * Refresh the access token
     * @param refreshToken refresh token of the user
     * @return [UserCredentials]
     */
    @Throws(ClientException::class, ApiException::class)
    fun refresh(refreshToken: String): UserCredentials {
        val request = RefreshRequest(refreshToken = refreshToken)

        val body = client.post("$apiEndpoint/refresh", Json.encodeToString(RefreshRequest.serializer(), request))

        return Json.decodeFromString(UserCredentials.serializer(), body)
    }

    companion object {
        /**
         * Compute base password hash
         * @param password password of the user
         * @param email email of the user
         */
        fun computeBasePasswordHash(password: String, email: String): String {
            val hash = Argon2idHasher.hash(password, email.encodeToByteArray())
            return Argon2id.toHexHash(hash)
        }

        /**
         * Compute final password hash
         * @param basePassword base password hash of the user
         * @param email email of the user
         */
        fun computeFinalPasswordHash(basePassword: String, email: String): String {
            return Pbkdf2(EncryptionKeyIterations).sha256(basePassword, email.encodeToByteArray())
        }
    }
}
