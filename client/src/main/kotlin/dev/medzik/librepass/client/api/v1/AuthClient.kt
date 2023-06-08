package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.AES
import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Curve25519
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.DEFAULT_API_URL
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.computeBasePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeFinalPasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeHashes
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import dev.medzik.librepass.types.api.auth.UserCredentials

/**
 * Auth Client for the LibrePass API. This client is used to register and login users.
 * @param apiUrl api url address (optional)
 */
class AuthClient(apiUrl: String = DEFAULT_API_URL) {
    companion object {
        const val API_ENDPOINT = "/api/v1/auth"
    }

    private val client = Client(apiUrl)

    /**
     * Register a new user
     * @param email user email address
     * @param password user password
     * @param passwordHint hint for the password (optional)
     */
    @Throws(ClientException::class, ApiException::class)
    fun register(email: String, password: String, passwordHint: String? = null) {
        // compute password hashes
        val passwordHashes = computeHashes(password, email)

        // generate Curve25519 keypair
        val keyPair = Curve25519.generateKeyPair()

        // encrypt private key with password hash
        val protectedPrivateKey = AES.encrypt(
            AES.GCM,
            passwordHashes.basePasswordHash.toHexHash(),
            keyPair.privateKey
        )

        val request = RegisterRequest(
            email = email,
            passwordHash = passwordHashes.finalPasswordHash,
            passwordHint = passwordHint,
            // Argon2id parameters
            parallelism = passwordHashes.basePasswordHash.parallelism,
            memory = passwordHashes.basePasswordHash.memory,
            iterations = passwordHashes.basePasswordHash.iterations,
            version = passwordHashes.basePasswordHash.version,
            // Curve25519 keypair
            publicKey = keyPair.publicKey,
            protectedPrivateKey = protectedPrivateKey
        )

        client.post("$API_ENDPOINT/register", JsonUtils.serialize(request))
    }

    /**
     * Get the argon2id parameters of a user (for login)
     * @param email user email
     * @return [UserArgon2idParameters]
     */
    @Throws(ClientException::class, ApiException::class)
    fun getUserArgon2idParameters(email: String): UserArgon2idParameters {
        val response = client.get("$API_ENDPOINT/userArgon2Parameters?email=$email")
        return JsonUtils.deserialize(response)
    }

    /**
     * Login a user
     * @param email user email
     * @param password user password
     * @return [UserCredentials]
     */
    @Throws(ClientException::class, ApiException::class)
    fun login(email: String, password: String): UserCredentials {
        // compute the base password hash
        val basePassword = computeBasePasswordHash(
            password = password,
            email = email,
            parameters = getUserArgon2idParameters(email)
        )

        return login(email, password, basePassword)
    }

    /**
     * Login a user.
     * @param email user email
     * @param password user password (not hashed)
     * @param basePassword base password hash (hashed)
     * @return [UserCredentials]
     */
    @Throws(ClientException::class, ApiException::class)
    fun login(email: String, password: String, basePassword: Argon2Hash): UserCredentials {
        // compute the final password, it is required since the earlier hash is used to encrypt the encryption key
        val finalPassword = computeFinalPasswordHash(
            password = password,
            basePassword = basePassword.toHexHash(),
        )

        val request = LoginRequest(
            email = email,
            passwordHash = finalPassword
        )

        val response = client.post("$API_ENDPOINT/login", JsonUtils.serialize(request))
        return JsonUtils.deserialize(response)
    }

    /**
     * Request password hint.
     * @param email user email
     */
    @Throws(ClientException::class, ApiException::class)
    fun requestPasswordHint(email: String) {
        client.get("$API_ENDPOINT/passwordHint?email=$email")
    }
}
