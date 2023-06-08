package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.AES
import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.DEFAULT_API_URL
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.computeBasePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeHashes
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import dev.medzik.librepass.types.api.user.ChangePasswordRequest
import dev.medzik.librepass.types.api.user.UserSecrets
import dev.medzik.librepass.types.api.user.UserSecretsResponse

/**
 * User Client for the LibrePass API. This client is used to manage user.
 * @param email user email
 * @param apiKey api key to use for authentication
 * @param apiUrl api url address (optional)
 */
class UserClient(
    private val email: String,
    apiKey: String,
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
     * @param parameters argon2id parameters of the new password
     */
    @Throws(ClientException::class, ApiException::class)
    fun changePassword(
        oldPassword: String,
        newPassword: String,
        parameters: UserArgon2idParameters? = null
    ) {
        // get the user secrets from the server
        val userSecrets = getSecrets(oldPassword)

        val argon2idParameters = parameters ?: AuthClient(apiUrl).getUserArgon2idParameters(email)

        // compute old password hashes
        val oldPasswordHashes = computeHashes(
            password = oldPassword,
            email = email,
        )

        // compute new password hashes
        val newPasswordHashes = computeHashes(
            password = newPassword,
            email = email,
        )

        // encrypt the private key with the new password
        val protectedPrivateKey = AES.encrypt(
            AES.GCM,
            newPasswordHashes.basePasswordHash.toHexHash(),
            userSecrets.privateKey
        )

        val request = ChangePasswordRequest(
            oldPassword = oldPasswordHashes.finalPasswordHash,
            newPassword = newPasswordHashes.finalPasswordHash,
            newProtectedPrivateKey = protectedPrivateKey,
            parallelism = argon2idParameters.parallelism,
            memory = argon2idParameters.memory,
            iterations = argon2idParameters.iterations,
            version = argon2idParameters.version
        )

        client.patch(
            "${API_ENDPOINT}/password",
            JsonUtils.serialize(request)
        )
    }

    /**
     * Get user secrets.
     * @param password user password
     * @return [UserSecrets]
     */
    @Throws(ClientException::class, ApiException::class)
    fun getSecrets(password: String): UserSecrets {
        val argon2idParameters = AuthClient(apiUrl = apiUrl).getUserArgon2idParameters(email)

        // compute base password
        val basePassword = computeBasePasswordHash(
            password = password,
            email = email,
            parameters = argon2idParameters
        )

        return getSecrets(basePassword)
    }

    /**
     * Get user secrets.
     * @param basePassword base password hash of the user password
     * @return [UserSecrets]
     */
    @Throws(ClientException::class, ApiException::class)
    fun getSecrets(basePassword: Argon2Hash): UserSecrets {
        val response = client.get("${API_ENDPOINT}/secrets")
        val userSecrets = JsonUtils.deserialize<UserSecretsResponse>(response)
        return userSecrets.decrypt(basePassword)
    }
}
