package dev.medzik.librepass.client.api

import dev.medzik.libcrypto.AES
import dev.medzik.libcrypto.Argon2
import dev.medzik.libcrypto.Curve25519
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.computePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeSecretKey
import dev.medzik.librepass.client.utils.Cryptography.computeSecretKeyFromPassword
import dev.medzik.librepass.client.utils.Cryptography.computeSharedKey
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.user.ChangePasswordCipherData
import dev.medzik.librepass.types.api.user.ChangePasswordRequest
import dev.medzik.librepass.types.api.user.SetupTwoFactorRequest
import dev.medzik.librepass.types.api.user.SetupTwoFactorResponse

/**
 * User Client for the LibrePass API. This client is used to manage user.
 * @param email user email
 * @param apiKey api key to use for authentication
 * @param apiUrl api url address (optional)
 */
class UserClient(
    private val email: String,
    private val apiKey: String,
    private val apiUrl: String = Server.PRODUCTION
) {
    companion object {
        private const val API_ENDPOINT = "/api/user"
    }

    private val client = Client(apiUrl, apiKey)

    /**
     * Change user password.
     * @param oldPassword old password
     * @param newPassword new password
     * @param newPasswordHint hint for the new password
     * @param argon2Function argon2 function for hashing the new password
     */
    @Throws(ClientException::class, ApiException::class)
    fun changePassword(
        oldPassword: String,
        newPassword: String,
        newPasswordHint: String? = null,
        argon2Function: Argon2 = Argon2(32, 3, 65536, 4)
    ) {
        val oldPreLogin = AuthClient(apiUrl).preLogin(email)

        // compute old secret key
        val oldSecretKey = computeSecretKeyFromPassword(
            email = email,
            password = oldPassword,
            argon2Function = oldPreLogin.toArgon2()
        )

        // compute new password hashes
        val newPasswordHash = computePasswordHash(
            email = email,
            password = newPassword,
            argon2Function = argon2Function
        )

        val newKeyPair = Curve25519.fromPrivateKey(newPasswordHash.toHexHash())

        // get server public key
        val serverPublicKey = oldPreLogin.serverPublicKey

        // compute shared key with a new private key and server public key
        val sharedKey = computeSharedKey(newKeyPair.privateKey, serverPublicKey)

        // compute new secret key
        val newSecretKey = computeSecretKey(newKeyPair)

        // re-encrypt ciphers data with new password
        val cipherClient = CipherClient(apiKey, apiUrl)
        val ciphers = mutableListOf<ChangePasswordCipherData>()
        cipherClient.getAll().forEach { cipher ->
            // decrypt cipher data with an old secret key
            val oldData = AES.decrypt(AES.GCM, oldSecretKey, cipher.protectedData)

            // encrypt cipher data with a new secret key
            val newData = AES.encrypt(AES.GCM, newSecretKey, oldData)

            ciphers += ChangePasswordCipherData(
                id = cipher.id,
                data = newData
            )
        }

        val request = ChangePasswordRequest(
            newPasswordHint = newPasswordHint,
            sharedKey = sharedKey,
            // Argon2id parameters
            parallelism = newPasswordHash.parallelism,
            memory = newPasswordHash.memory,
            iterations = newPasswordHash.iterations,
            // Curve25519 public key
            newPublicKey = newKeyPair.publicKey,
            // ciphers data re-encrypted with new password
            ciphers = ciphers
        )

        client.patch(
            "$API_ENDPOINT/password",
            JsonUtils.serialize(request)
        )
    }

    @Throws(ClientException::class, ApiException::class)
    fun setupTwoFactor(
        email: String,
        password: String,
        secret: String,
        code: String
    ): SetupTwoFactorResponse {
        val preLogin = AuthClient(apiUrl).preLogin(email)

        val passwordHash = computePasswordHash(
            email = email,
            password = password,
            argon2Function = preLogin.toArgon2()
        )

        val keyPair = Curve25519.fromPrivateKey(passwordHash.toHexHash())
        val serverPublicKey = preLogin.serverPublicKey
        val sharedKey = computeSharedKey(keyPair.privateKey, serverPublicKey)

        val request = SetupTwoFactorRequest(
            sharedKey = sharedKey,
            secret = secret,
            code = code
        )

        val response = client.post(
            "$API_ENDPOINT/setup/2fa",
            JsonUtils.serialize(request)
        )
        return JsonUtils.deserialize(response)
    }
}
