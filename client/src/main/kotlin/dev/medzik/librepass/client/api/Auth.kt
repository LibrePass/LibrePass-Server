package dev.medzik.librepass.client.api

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.X25519
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.*
import dev.medzik.librepass.utils.Cryptography.computeAesKey
import dev.medzik.librepass.utils.Cryptography.computePasswordHash
import dev.medzik.librepass.utils.Cryptography.computeSharedKey
import dev.medzik.librepass.utils.fromHex
import dev.medzik.librepass.utils.toHex
import java.util.*

/**
 * Authentication client for user authentications.
 *
 * @param apiUrl server api url (default: official production server)
 */
class AuthClient(apiUrl: String = Server.PRODUCTION) {
    companion object {
        private const val API_ENDPOINT = "/api/auth"
    }

    private val client = Client(apiUrl)

    /**
     * Creates a new user account.
     *
     * @param email email address of the user
     * @param password user password
     * @param passwordHint password hint for the password (optional but recommended)
     */
    @Throws(ClientException::class, ApiException::class)
    fun register(
        email: String,
        password: String,
        passwordHint: String? = null
    ) {
        // get required parameters from server for computing password hash
        val serverPreLogin = preLogin()
        val argon2Function = serverPreLogin.toArgon2()

        // compute password hash
        val passwordHash = computePasswordHash(password, email, argon2Function)
        // set password hash as a x25519 private key
        val privateKey = passwordHash.hash
        // compute public key from the private key
        val publicKey = X25519.publicFromPrivate(privateKey)

        // compute shared key for "handshake"
        val serverPublicKey = serverPreLogin.serverPublicKey.fromHex()
        val sharedKey = computeSharedKey(privateKey, serverPublicKey).toHex()

        val request = RegisterRequest(
            email = email,
            passwordHint = passwordHint,
            sharedKey = sharedKey,
            parallelism = passwordHash.parallelism,
            memory = passwordHash.memory,
            iterations = passwordHash.iterations,
            publicKey = publicKey.toHex()
        )

        client.post("$API_ENDPOINT/register", JsonUtils.serialize(request))
    }

    /**
     * Gets the parameters required for authentication.
     *
     * @param email user's email address
     * @return [PreLoginResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun preLogin(email: String = ""): PreLoginResponse {
        val response = client.get("$API_ENDPOINT/preLogin?email=$email")
        return JsonUtils.deserialize(response)
    }

    /**
     * Logging the user using password.
     *
     * @param email email address of the user
     * @param password user password
     * @return authentication credentials of the user
     */
    @Throws(ClientException::class, ApiException::class)
    fun login(
        email: String,
        password: String
    ): UserCredentials {
        val preLoginData = preLogin(email)

        val passwordHash = computePasswordHash(
            password = password,
            email = email,
            argon2Function = preLoginData.toArgon2()
        )

        return login(email, passwordHash, preLoginData)
    }

    /**
     * Login the user using [passwordHash] (private key).
     *
     * @param email email address of the user
     * @param passwordHash hash of user password
     * @param preLogin pre-login response data (if not provided, sends request to the API)
     * @return authentication credentials of the user
     */
    @Throws(ClientException::class, ApiException::class)
    fun login(
        email: String,
        passwordHash: Argon2Hash,
        preLogin: PreLoginResponse? = null
    ): UserCredentials {
        // get server public key
        val serverPublicKey = preLogin?.serverPublicKey ?: preLogin(email).serverPublicKey

        // user keypair
        val privateKey = passwordHash.hash
        val publicKey = X25519.publicFromPrivate(privateKey)
        // compute shared key for "handshake"
        val sharedKey = computeSharedKey(privateKey, serverPublicKey.fromHex())

        val request = LoginRequest(
            email = email,
            sharedKey = sharedKey.toHex(),
        )

        // send login request and extract user credentials
        val responseBody = client.post("$API_ENDPOINT/oauth?grantType=login", JsonUtils.serialize(request))
        val userCredentialsResponse = JsonUtils.deserialize<UserCredentialsResponse>(responseBody)

        // compute key for vault encryption
        val aesKey = computeAesKey(privateKey)

        return UserCredentials(
            userId = userCredentialsResponse.userId,
            apiKey = userCredentialsResponse.apiKey,
            apiKeyVerified = userCredentialsResponse.verified,
            publicKey = publicKey.toHex(),
            privateKey = privateKey.toHex(),
            aesKey = aesKey.toHex()
        )
    }

    /**
     * Verifies the API key if the user has two-factor authentication enabled.
     *
     * @param apiKey api key returned by [login] endpoint
     * @param code one-time code
     */
    @Throws(ClientException::class, ApiException::class)
    fun loginTwoFactor(
        apiKey: String,
        code: String
    ) {
        val request = TwoFactorRequest(
            apiKey = apiKey,
            code = code
        )

        client.post("$API_ENDPOINT/oauth?grantType=2fa", JsonUtils.serialize(request))
    }

    /**
     * Sends an email with user password hint.
     *
     * @param email email address of the user
     */
    @Throws(ClientException::class, ApiException::class)
    fun requestPasswordHint(email: String) {
        client.get("$API_ENDPOINT/passwordHint?email=$email")
    }

    /**
     * Resends a user verification email.
     *
     * @param email email address of the user
     */
    @Throws(ClientException::class, ApiException::class)
    fun resendVerificationEmail(email: String) {
        client.get("$API_ENDPOINT/resendVerificationEmail?email=$email")
    }
}

/**
 * User credentials for authentication.
 *
 * @property userId user unique identifier
 * @property apiKey api key
 * @property apiKeyVerified whether the api key is verified (true) or not (false) - false only when the user
 * has enabled two-factor authentication but the api key is not verified using [AuthClient.loginTwoFactor] endpoint.
 * @property publicKey public key of the user
 * @property privateKey private key of the user
 * @property aesKey key for vault encryption
 */
data class UserCredentials(
    val userId: UUID,
    val apiKey: String,
    val apiKeyVerified: Boolean,
    val publicKey: String,
    val privateKey: String,
    val aesKey: String
)
