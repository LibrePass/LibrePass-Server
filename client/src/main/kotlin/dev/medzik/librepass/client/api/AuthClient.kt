package dev.medzik.librepass.client.api

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Curve25519
import dev.medzik.libcrypto.Curve25519KeyPair
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.DEFAULT_API_URL
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.computePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeSecretKey
import dev.medzik.librepass.client.utils.Cryptography.computeSharedKey
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.auth.*
import java.util.*

/**
 * Auth Client for the LibrePass API. This client is used to register and login users.
 * @param apiUrl api url address (optional)
 */
@Suppress("unused")
class AuthClient(apiUrl: String = DEFAULT_API_URL) {
    companion object {
        private const val API_ENDPOINT = "/api/auth"
    }

    private val client = Client(apiUrl)

    @Throws(ClientException::class, ApiException::class)
    fun register(email: String, password: String, passwordHint: String? = null) {
        val serverPreLogin = preLogin("")

        val passwordHash = computePasswordHash(password, email, serverPreLogin.toArgon2())
        val keyPair = Curve25519.fromPrivateKey(passwordHash.toHexHash())

        // compute shared key
        val sharedKey = computeSharedKey(keyPair.privateKey, serverPreLogin.serverPublicKey)

        val request = RegisterRequest(
            email = email,
            passwordHint = passwordHint,
            sharedKey = sharedKey,
            // Argon2id parameters
            parallelism = passwordHash.parallelism,
            memory = passwordHash.memory,
            iterations = passwordHash.iterations,
            // Curve25519 keypair
            publicKey = keyPair.publicKey
        )

        client.post("$API_ENDPOINT/register", JsonUtils.serialize(request))
    }

    @Throws(ClientException::class, ApiException::class)
    fun preLogin(email: String): PreLoginResponse {
        val response = client.get("$API_ENDPOINT/preLogin?email=$email")
        return JsonUtils.deserialize(response)
    }

    @Throws(ClientException::class, ApiException::class)
    fun login(email: String, password: String): UserCredentials {
        val preLoginData = preLogin(email)

        val passwordHash = computePasswordHash(
            password = password,
            email = email,
            argon2Function = preLoginData.toArgon2()
        )

        return login(email, passwordHash, preLoginData)
    }

    @Throws(ClientException::class, ApiException::class)
    fun login(email: String, passwordHash: Argon2Hash, preLogin: PreLoginResponse? = null): UserCredentials {
        val serverPublicKey = preLogin?.serverPublicKey ?: preLogin(email).serverPublicKey

        val keyPair = Curve25519.fromPrivateKey(passwordHash.toHexHash())
        val sharedKey = computeSharedKey(keyPair.privateKey, serverPublicKey)

        val request = LoginRequest(
            email = email,
            sharedKey = sharedKey,
        )

        val responseBody = client.post("$API_ENDPOINT/oauth?grantType=login", JsonUtils.serialize(request))
        val response = JsonUtils.deserialize<UserCredentialsResponse>(responseBody)

        return UserCredentials(
            userId = response.userId,
            apiKey = response.apiKey,
            keyPair = keyPair,
            secretKey = computeSecretKey(keyPair)
        )
    }

    @Throws(ClientException::class, ApiException::class)
    fun requestPasswordHint(email: String) {
        client.get("$API_ENDPOINT/passwordHint?email=$email")
    }
}

data class UserCredentials(
    val userId: UUID,
    val apiKey: String,
    val keyPair: Curve25519KeyPair,
    val secretKey: String
)
