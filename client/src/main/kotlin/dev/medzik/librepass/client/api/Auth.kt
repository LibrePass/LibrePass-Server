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
import dev.medzik.librepass.types.api.*
import dev.medzik.librepass.utils.fromHexString
import dev.medzik.librepass.utils.toHexString
import java.util.*

/**
 * Auth Client for authenticating users with the LibrePass API.
 *
 * @param apiUrl The API url address (default official production server)
 */
class AuthClient(apiUrl: String = Server.PRODUCTION) {
    companion object {
        private const val API_ENDPOINT = "/api/auth"
    }

    private val client = Client(apiUrl)

    /**
     * Register a new user.
     *
     * @param email The email address of the user.
     * @param password The user password.
     * @param passwordHint The password hint for the password. (Optional but recommended)
     */
    @Throws(ClientException::class, ApiException::class)
    fun register(
        email: String,
        password: String,
        passwordHint: String? = null
    ) {
        // get required parameters from server for computing password hash
        val serverPreLogin = preLogin("")
        val argon2Hasher = serverPreLogin.toArgon2()

        val passwordArgon2Hash = computePasswordHash(password, email, argon2Hasher)
        val privateKey = passwordArgon2Hash.hash
        val publicKey = X25519.publicFromPrivate(privateKey)

        val serverPublicKey = serverPreLogin.serverPublicKey.fromHexString()
        val sharedKey = computeSharedKey(privateKey, serverPublicKey).toHexString()

        val request =
            RegisterRequest(
                email = email,
                passwordHint = passwordHint,
                sharedKey = sharedKey,
                parallelism = passwordArgon2Hash.parallelism,
                memory = passwordArgon2Hash.memory,
                iterations = passwordArgon2Hash.iterations,
                publicKey = publicKey.toHexString()
            )

        client.post("$API_ENDPOINT/register", JsonUtils.serialize(request))
    }

    /**
     * Gets the parameters required for authentication.
     *
     * @param email The email address of the user.
     * @return The parameters.
     */
    @Throws(ClientException::class, ApiException::class)
    fun preLogin(email: String): PreLoginResponse {
        val response = client.get("$API_ENDPOINT/preLogin?email=$email")
        return JsonUtils.deserialize(response)
    }

    /**
     * Login the user using password.
     *
     * @param email The email address of the user.
     * @param password The user password.
     * @return The authentication credentials of the user.
     */
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

    /**
     * Login the user using [passwordHash] (private key).
     *
     * @param email The email address of the user.
     * @param passwordHash The password hash.
     * @param preLogin The pre-login response data. (Optional, if not provided, sends request to the API)
     * @return The authentication credentials of the user.
     */
    @Throws(ClientException::class, ApiException::class)
    fun login(
        email: String,
        passwordHash: Argon2Hash,
        preLogin: PreLoginResponse? = null
    ): UserCredentials {
        val serverPublicKey = preLogin?.serverPublicKey ?: preLogin(email).serverPublicKey
        val privateKey = passwordHash.hash
        val publicKey = X25519.publicFromPrivate(privateKey)
        val sharedKey = computeSharedKey(privateKey, serverPublicKey.fromHexString())

        val request =
            LoginRequest(
                email = email,
                sharedKey = sharedKey.toHexString(),
            )

        val responseBody = client.post("$API_ENDPOINT/oauth?grantType=login", JsonUtils.serialize(request))
        val userCredentialsResponse = JsonUtils.deserialize<UserCredentialsResponse>(responseBody)
        val secretKey = computeSecretKey(privateKey)

        return UserCredentials(
            userId = userCredentialsResponse.userId,
            apiKey = userCredentialsResponse.apiKey,
            apiKeyVerified = userCredentialsResponse.verified,
            publicKey = publicKey.toHexString(),
            privateKey = privateKey.toHexString(),
            secretKey = secretKey.toHexString()
        )
    }

    /**
     * Verify the API key if the user has two-factor authentication enabled.
     *
     * @param apiKey The API key.
     * @param code The OTP code.
     */
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

    /**
     * Request a password hint, will be sent to your email address.
     *
     * @param email The email address of the user.
     */
    @Throws(ClientException::class, ApiException::class)
    fun requestPasswordHint(email: String) {
        client.get("$API_ENDPOINT/passwordHint?email=$email")
    }
}

/**
 * Credentials of the user.
 *
 * @property userId The identifier of the user.
 * @property apiKey The user API key.
 * @property apiKeyVerified If false, you need to authenticate the API key with OTP code to use the API.
 *  (Only if the user enabled 2FA authentication)
 * @property publicKey The user's public key.
 * @property privateKey The user's private key.
 * @property secretKey The user's secret key.
 */
data class UserCredentials(
    val userId: UUID,
    val apiKey: String,
    val apiKeyVerified: Boolean,
    val publicKey: String,
    val privateKey: String,
    val secretKey: String
)
