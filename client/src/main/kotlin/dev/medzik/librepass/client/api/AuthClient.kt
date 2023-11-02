package dev.medzik.librepass.client.api

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.Hex
import dev.medzik.libcrypto.X25519
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.computePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeSecretKey
import dev.medzik.librepass.client.utils.Cryptography.computeSharedKey
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.*
import dev.medzik.librepass.utils.fromHexString
import dev.medzik.librepass.utils.toHexString
import java.util.*

/**
 * Auth Client for the LibrePass API. This client is used to register and login users.
 * @param apiUrl api url address (optional)
 */
@Suppress("unused")
class AuthClient(apiUrl: String = Server.PRODUCTION) {
    companion object {
        private const val API_ENDPOINT = "/api/auth"
    }

    private val client = Client(apiUrl)

    @Throws(ClientException::class, ApiException::class)
    fun register(
        email: String,
        password: String,
        passwordHint: String? = null
    ) {
        val serverPreLogin = preLogin("")

        val passwordHash = computePasswordHash(password, email, serverPreLogin.toArgon2())
        val publicKey = X25519.publicFromPrivate(passwordHash.hash)

        // compute shared key
        val sharedKey = computeSharedKey(passwordHash.hash, serverPreLogin.serverPublicKey.fromHexString())

        val request =
            RegisterRequest(
                email = email,
                passwordHint = passwordHint,
                sharedKey = sharedKey.toHexString(),
                // Argon2id parameters
                parallelism = passwordHash.parallelism,
                memory = passwordHash.memory,
                iterations = passwordHash.iterations,
                // Curve25519 public key
                publicKey = publicKey.toHexString()
            )

        client.post("$API_ENDPOINT/register", JsonUtils.serialize(request))
    }

    @Throws(ClientException::class, ApiException::class)
    fun preLogin(email: String): PreLoginResponse {
        val response = client.get("$API_ENDPOINT/preLogin?email=$email")
        return JsonUtils.deserialize(response)
    }

    @Throws(ClientException::class, ApiException::class)
    fun login(
        email: String,
        password: String
    ): UserCredentials {
        val preLoginData = preLogin(email)

        val passwordHash =
            computePasswordHash(
                password = password,
                email = email,
                argon2Function = preLoginData.toArgon2()
            )

        return login(email, passwordHash, preLoginData)
    }

    @Throws(ClientException::class, ApiException::class)
    fun login(
        email: String,
        passwordHash: Argon2Hash,
        preLogin: PreLoginResponse? = null
    ): UserCredentials {
        val serverPublicKey = preLogin?.serverPublicKey ?: preLogin(email).serverPublicKey

        val publicKey = X25519.publicFromPrivate(passwordHash.hash)
        val sharedKey = computeSharedKey(passwordHash.hash, serverPublicKey.fromHexString())

        val request =
            LoginRequest(
                email = email,
                sharedKey = sharedKey.toHexString(),
            )

        val responseBody = client.post("$API_ENDPOINT/oauth?grantType=login", JsonUtils.serialize(request))
        val response = JsonUtils.deserialize<UserCredentialsResponse>(responseBody)

        val secretKey = computeSecretKey(passwordHash.hash)

        return UserCredentials(
            userId = response.userId,
            apiKey = response.apiKey,
            apiKeyVerified = response.verified,
            publicKey = Hex.encode(publicKey),
            privateKey = Hex.encode(passwordHash.hash),
            secretKey = secretKey.toHexString()
        )
    }

    @Throws(ClientException::class, ApiException::class)
    fun loginTwoFactor(
        apiKey: String,
        code: String
    ) {
        val request =
            TwoFactorRequest(
                apiKey = apiKey,
                code = code
            )

        client.post("$API_ENDPOINT/oauth?grantType=2fa", JsonUtils.serialize(request))
    }

    @Throws(ClientException::class, ApiException::class)
    fun requestPasswordHint(email: String) {
        client.get("$API_ENDPOINT/passwordHint?email=$email")
    }
}

data class UserCredentials(
    val userId: UUID,
    val apiKey: String,
    val apiKeyVerified: Boolean,
    val publicKey: String,
    val privateKey: String,
    val secretKey: String
)
