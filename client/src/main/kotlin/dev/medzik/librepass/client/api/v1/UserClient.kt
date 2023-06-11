package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.AES
import dev.medzik.libcrypto.Curve25519
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.DEFAULT_API_URL
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.DefaultArgon2idParameters
import dev.medzik.librepass.client.utils.Cryptography.computePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeSecretKey
import dev.medzik.librepass.client.utils.Cryptography.computeSecretKeyFromPassword
import dev.medzik.librepass.client.utils.Cryptography.computeSharedKey
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import dev.medzik.librepass.types.api.user.ChangePasswordCipherData
import dev.medzik.librepass.types.api.user.ChangePasswordRequest

/**
 * User Client for the LibrePass API. This client is used to manage user.
 * @param email user email
 * @param apiKey api key to use for authentication
 * @param apiUrl api url address (optional)
 */
class UserClient(
    private val email: String,
    private val apiKey: String,
    private val apiUrl: String = DEFAULT_API_URL
) {
    companion object {
        const val API_ENDPOINT = "/api/v1/user"
    }

    private val client = Client(apiUrl, apiKey)

    /**
     * Change user password.
     * @param oldPassword old password
     * @param newPassword new password
     * @param newPasswordHint hint for the new password
     * @param parameters argon2id parameters of the new password
     */
    @Throws(ClientException::class, ApiException::class)
    fun changePassword(
        oldPassword: String,
        newPassword: String,
        newPasswordHint: String? = null,
        parameters: UserArgon2idParameters = DefaultArgon2idParameters
    ) {
        val oldArgon2idParameters = AuthClient(apiUrl).getUserArgon2idParameters(email)

        // compute old secret key
        val oldSecretKey = computeSecretKeyFromPassword(email, oldPassword, oldArgon2idParameters)

        // compute new password hashes
        val newPasswordHash = computePasswordHash(
            password = newPassword,
            email = email,
            parameters = parameters
        ).toHexHash()

        val newKeyPair = Curve25519.fromPrivateKey(newPasswordHash)

        // get server public key
        val serverPublicKey = AuthClient(apiUrl).getServerPublicKey()

        // compute shared key with new private key and server public key
        val sharedKey = computeSharedKey(newKeyPair.privateKey, serverPublicKey)

        // compute new secret key
        val newSecretKey = computeSecretKey(newKeyPair)

        // re-encrypt ciphers data with new password
        val cipherClient = CipherClient(apiKey, apiUrl)
        val ciphers = mutableListOf<ChangePasswordCipherData>()
        cipherClient.getAll().forEach { cipher ->
            // decrypt cipher data with old secret key
            val oldData = AES.decrypt(AES.GCM, oldSecretKey, cipher.protectedData)

            // encrypt cipher data with new secret key
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
            parallelism = parameters.parallelism,
            memory = parameters.memory,
            iterations = parameters.iterations,
            version = parameters.version,
            // Curve25519 public key
            newPublicKey = newKeyPair.publicKey,
            // ciphers data re-encrypted with new password
            ciphers = ciphers
        )

        client.patch(
            "${API_ENDPOINT}/password",
            JsonUtils.serialize(request)
        )
    }
}
