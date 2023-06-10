package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Curve25519
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.DEFAULT_API_URL
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.computePasswordHash
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.auth.*

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
     * Get the public key of the server.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getServerPublicKey(): ServerPublicKey {
        val response = client.get("$API_ENDPOINT/serverPublicKey")
        return JsonUtils.deserialize(response)
    }

    /**
     * Register a new user.
     * @param email user email address
     * @param password user password
     * @param passwordHint hint for the password (optional)
     */
    @Throws(ClientException::class, ApiException::class)
    fun register(email: String, password: String, passwordHint: String? = null) {
        // compute password hash
        val passwordHash = computePasswordHash(password, email)

        // compute Curve25519 public key using base password hash as private key
        val publicKey = Curve25519.fromPrivateKey(passwordHash.toHexHash())

        // get server public key for shared key computation
        val serverPublicKey = getServerPublicKey()

        // compute shared key
        val sharedKey = Curve25519.computeSharedSecret(serverPublicKey.publicKey, publicKey.publicKey)

        val request = RegisterRequest(
            email = email,
            passwordHint = passwordHint,
            sharedKey = sharedKey,
            // Argon2id parameters
            parallelism = passwordHash.parallelism,
            memory = passwordHash.memory,
            iterations = passwordHash.iterations,
            version = passwordHash.version,
            // Curve25519 keypair
            publicKey = publicKey.publicKey
        )

        client.post("$API_ENDPOINT/register", JsonUtils.serialize(request))
    }

    /**
     * Get the argon2id parameters of a user (for login).
     * @param email user email
     * @return [UserArgon2idParameters]
     */
    @Throws(ClientException::class, ApiException::class)
    fun getUserArgon2idParameters(email: String): UserArgon2idParameters {
        val response = client.get("$API_ENDPOINT/userArgon2Parameters?email=$email")
        return JsonUtils.deserialize(response)
    }

    /**
     * Login a user.
     * @param email user email
     * @param password user password
     * @return [UserCredentials]
     */
    @Throws(ClientException::class, ApiException::class)
    fun login(email: String, password: String): UserCredentials {
        val basePassword = computePasswordHash(
            password = password,
            email = email,
            parameters = getUserArgon2idParameters(email)
        )

        return login(email, basePassword)
    }

    /**
     * Login a user.
     * @param email user email
     * @param passwordHash hashed password
     * @return [UserCredentials]
     */
    @Throws(ClientException::class, ApiException::class)
    fun login(email: String, passwordHash: Argon2Hash): UserCredentials {
        // compute Curve25519 public key using base password hash as private key
        val publicKey = Curve25519.fromPrivateKey(passwordHash.toHexHash())

        // get server public key for shared key computation
        val serverPublicKey = getServerPublicKey()

        // compute shared key
        val sharedKey = Curve25519.computeSharedSecret(serverPublicKey.publicKey, publicKey.publicKey)

        val request = LoginRequest(
            email = email,
            sharedKey = sharedKey,
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
