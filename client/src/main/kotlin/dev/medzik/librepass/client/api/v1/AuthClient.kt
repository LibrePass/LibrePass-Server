package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.*
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.api.v1.AuthClient.Companion.computeBasePasswordHash
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import dev.medzik.librepass.types.api.auth.UserCredentials
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Hex

const val EncryptionKeyIterations = 500

val DefaultArgon2idParameters = UserArgon2idParameters(
    parallelism = 3,
    memory = 65536, // 64MB
    iterations = 4,
    version = 19
)

interface AuthClient {
    /**
     * Register a new user
     * @param email email of the user
     * @param password password of the user
     * @param passwordHint password hint of the user (optional)
     */
    @Throws(ClientException::class, ApiException::class)
    fun register(email: String, password: String, passwordHint: String? = null)

    /**
     * Get the argon2id parameters of a user (for login)
     * @param email email of the user
     * @return [UserArgon2idParameters]
     */
    @Throws(ClientException::class, ApiException::class)
    fun getUserArgon2idParameters(email: String): UserArgon2idParameters

    /**
     * Login a user
     * @param email email of the user
     * @param password password of the user
     * @return [UserCredentials]
     */
    @Throws(ClientException::class, ApiException::class)
    fun login(email: String, password: String): UserCredentials

    /**
     * Login a user
     * @param email email of the user
     * @param basePassword base password of the user
     * @return [UserCredentials]
     */
    @Throws(ClientException::class, ApiException::class)
    fun login(email: String, basePassword: Argon2Hash): UserCredentials

    companion object {
        /**
         * Compute base password hash
         * @param password password of the user
         * @param email email of the user
         * @param parameters argon2id parameters
         */
        fun computeBasePasswordHash(
            password: String,
            email: String,
            parameters: UserArgon2idParameters = DefaultArgon2idParameters
        ): Argon2Hash {
            return parameters
                .toHashingFunction()
                .hash(password, email.toByteArray())
        }
    }
}

fun AuthClient(apiUrl: String = Client.DefaultApiUrl): AuthClient = AuthClientImpl(apiUrl)

private class AuthClientImpl(apiUrl: String) : AuthClient {
    private val apiEndpoint = "/api/v1/auth"

    private val client = Client(null, apiUrl)

    override fun register(
        email: String,
        password: String,
        passwordHint: String?
    ) {
        // compute the base password hash
        val basePassword = computeBasePasswordHash(password, email)
        val basePasswordHex = basePassword.toHexHash()

        // compute the final password, it is required since the earlier hash is used to encrypt the encryption key
        val finalPassword = computeFinalPasswordHash(basePasswordHex, email)

        // create a random byte array with 16 bytes and encode it to hex string,
        val encryptionKeyBase = Pbkdf2(EncryptionKeyIterations)
            .sha256(Hex.encodeHexString(Salt.generate(16)), Salt.generate(16))
        val encryptionKey = AesCbc.encrypt(encryptionKeyBase, basePasswordHex)

        // generate a new rsa keypair for the user
        val rsaKeypair = RSA.generateKeyPair(2048)

        // get the public and private key as string
        val rsaPublicKey = RSA.KeyUtils.getPublicKeyString(rsaKeypair.public)
        val rsaPrivateKey = RSA.KeyUtils.getPrivateKeyString(rsaKeypair.private)

        // encrypt private key with the encryption key
        val encryptedRsaPrivateKey = AesCbc.encrypt(rsaPrivateKey, encryptionKeyBase)

        val request = RegisterRequest(
            email = email,
            password = finalPassword,
            passwordHint = passwordHint,
            encryptionKey = encryptionKey,
            // argon2id parameters
            parallelism = basePassword.parallelism,
            memory = basePassword.memory,
            iterations = basePassword.iterations,
            version = basePassword.version,
            // rsa keypair
            publicKey = rsaPublicKey,
            privateKey = encryptedRsaPrivateKey
        )

        client.post("$apiEndpoint/register", Json.encodeToString(RegisterRequest.serializer(), request))
    }

    override fun getUserArgon2idParameters(email: String): UserArgon2idParameters {
        val body = client.get("$apiEndpoint/userArgon2Parameters?email=$email")
        return Json.decodeFromString(UserArgon2idParameters.serializer(), body)
    }

    override fun login(
        email: String,
        password: String
    ): UserCredentials {
        // compute the base password hash
        val basePassword = computeBasePasswordHash(
            password = password,
            email = email,
            parameters = getUserArgon2idParameters(email)
        )

        return login(email, basePassword)
    }

    override fun login(
        email: String,
        basePassword: Argon2Hash
    ): UserCredentials {
        // compute the final password, it is required since the earlier hash is used to encrypt the encryption key
        val finalPassword = computeFinalPasswordHash(
            basePassword = basePassword.toHexHash(),
            email = email
        )

        val request = LoginRequest(
            email = email,
            password = finalPassword
        )

        val body = client.post("$apiEndpoint/login", Json.encodeToString(LoginRequest.serializer(), request))

        return Json.decodeFromString(UserCredentials.serializer(), body)
    }

    /**
     * Compute final password hash
     * @param basePassword base password hash of the user
     * @param email email of the user
     */
    private fun computeFinalPasswordHash(
        basePassword: String,
        email: String
    ): String = Pbkdf2(EncryptionKeyIterations)
        .sha256(basePassword, email.encodeToByteArray())
}
