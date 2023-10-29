package dev.medzik.librepass.client.api

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.X25519
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.computePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeSecretKey
import dev.medzik.librepass.client.utils.Cryptography.computeSharedKey
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.auth.*
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
            publicKey = publicKey,
            privateKey = passwordHash.hash,
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
    val publicKey: ByteArray,
    val privateKey: ByteArray,
    val secretKey: String
) {
    // Auto-Generated functions

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserCredentials

        if (userId != other.userId) return false
        if (apiKey != other.apiKey) return false
        if (apiKeyVerified != other.apiKeyVerified) return false
        if (!publicKey.contentEquals(other.publicKey)) return false
        if (!privateKey.contentEquals(other.privateKey)) return false
        if (secretKey != other.secretKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + apiKey.hashCode()
        result = 31 * result + apiKeyVerified.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + privateKey.contentHashCode()
        result = 31 * result + secretKey.hashCode()
        return result
    }
}
