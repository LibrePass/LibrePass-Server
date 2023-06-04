package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.DEFAULT_API_URL
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.computeBasePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeHashes
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import dev.medzik.librepass.types.api.user.ChangePasswordRequest
import dev.medzik.librepass.types.api.user.UserSecretsResponse
import dev.medzik.librepass.types.utils.JsonUtils

/**
 * User API client.
 * @param email The email of the user.
 * @param accessToken The access token to use for authentication.
 * @param apiUrl The API URL to use. Defaults to [DEFAULT_API_URL].
 */
class UserClient(
    private val email: String,
    accessToken: String,
    private val apiUrl: String = DEFAULT_API_URL
) {
    companion object {
        const val API_ENDPOINT = "/api/v1/user"
    }

    private val client = Client(apiUrl, accessToken)

    /**
     * Change user password.
     * @param oldPassword Old password.
     * @param newPassword New password.
     * @param parameters Argon2id parameters.
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

        // encrypt the new secrets
        val newSecrets = userSecrets.encrypt(newPasswordHashes.basePasswordHash)

        val request = ChangePasswordRequest(
            oldPassword = oldPasswordHashes.finalPasswordHash,
            newPassword = newPasswordHashes.finalPasswordHash,
            newProtectedEncryptionKey = newSecrets.encryptionKey,
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
     * @param password User password.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getSecrets(password: String): UserSecretsResponse {
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
     * @param basePassword User base password hash.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getSecrets(basePassword: Argon2Hash): UserSecretsResponse {
        val response = client.get("${API_ENDPOINT}/secrets")
        val userSecrets = JsonUtils.deserialize<UserSecretsResponse>(response)
        return userSecrets.decrypt(basePassword)
    }
}
