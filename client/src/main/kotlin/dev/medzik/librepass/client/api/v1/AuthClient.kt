package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.AesCbc
import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.RSA
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.DEFAULT_API_URL
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.RSAKeySize
import dev.medzik.librepass.client.utils.Cryptography.computeBasePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeFinalPasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeHashes
import dev.medzik.librepass.client.utils.Cryptography.createEncryptionKey
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import dev.medzik.librepass.types.api.auth.UserCredentials
import kotlinx.serialization.json.Json

/**
 * Auth Client for the LibrePass API. This client is used to register and login users.
 * @param apiUrl The API URL to use. Defaults to [DEFAULT_API_URL].
 */
class AuthClient(apiUrl: String = DEFAULT_API_URL) {
    companion object {
        const val API_ENDPOINT = "/api/v1/auth"
    }

    private val client = Client(null, apiUrl)

    /**
     * Register a new user
     * @param email email of the user
     * @param password password of the user
     * @param passwordHint password hint of the user (optional)
     */
    @Throws(ClientException::class, ApiException::class)
    fun register(email: String, password: String, passwordHint: String? = null) {
        // compute password hashes
        val passwordHashes = computeHashes(password, email)

        // create a random encryption key
        val encryptionKey = createEncryptionKey()
        // encrypt the encryption key with the base password hash
        val encryptedEncryptionKey = AesCbc.encrypt(encryptionKey, passwordHashes.basePasswordHashString)

        // generate a new rsa keypair for the user
        val rsaKeypair = RSA.generateKeyPair(RSAKeySize)

        // get the public and private key as string
        val rsaPublicKey = RSA.KeyUtils.getPublicKeyString(rsaKeypair.public)
        val rsaPrivateKey = RSA.KeyUtils.getPrivateKeyString(rsaKeypair.private)

        // encrypt private key with the encryption key
        val encryptedRsaPrivateKey = AesCbc.encrypt(rsaPrivateKey, encryptionKey)

        val request = RegisterRequest(
            email = email,
            password = passwordHashes.finalPasswordHash,
            passwordHint = passwordHint,
            encryptionKey = encryptedEncryptionKey,
            // argon2id parameters
            parallelism = passwordHashes.basePasswordHash.parallelism,
            memory = passwordHashes.basePasswordHash.memory,
            iterations = passwordHashes.basePasswordHash.iterations,
            version = passwordHashes.basePasswordHash.version,
            // rsa keypair
            publicKey = rsaPublicKey,
            privateKey = encryptedRsaPrivateKey
        )

        client.post("$API_ENDPOINT/register", Json.encodeToString(RegisterRequest.serializer(), request))
    }

    /**
     * Get the argon2id parameters of a user (for login)
     * @param email email of the user
     * @return [UserArgon2idParameters]
     */
    @Throws(ClientException::class, ApiException::class)
    fun getUserArgon2idParameters(email: String): UserArgon2idParameters {
        val body = client.get("$API_ENDPOINT/userArgon2Parameters?email=$email")
        return Json.decodeFromString(UserArgon2idParameters.serializer(), body)
    }

    /**
     * Login a user
     * @param email email of the user
     * @param password password of the user
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
     * Login a user
     * @param email email of the user
     * @param password password of the user (not hashed)
     * @param basePassword base password of the user (hashed)
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
            password = finalPassword
        )

        val body = client.post("$API_ENDPOINT/login", Json.encodeToString(LoginRequest.serializer(), request))

        return Json.decodeFromString(UserCredentials.serializer(), body)
    }
}
