package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.Cryptography.computeBasePasswordHash
import dev.medzik.librepass.client.utils.Cryptography.computeFinalPasswordHash
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import dev.medzik.librepass.types.api.user.ChangePasswordRequest
import dev.medzik.librepass.types.api.user.UserSecretsResponse
import kotlinx.serialization.json.Json

class UserClient(
    private val email: String,
    accessToken: String,
    private val apiUrl: String = Client.DefaultApiUrl
) {
    companion object {
        const val API_ENDPOINT = "/api/v1/user"
    }

    private val client = Client(accessToken, apiUrl)

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

        val argon2idParameters = parameters ?: AuthClient(apiUrl = apiUrl).getUserArgon2idParameters(email)

        // compute old password hashes
        val oldBasePassword = computeBasePasswordHash(
            password = oldPassword,
            email = email,
            parameters = argon2idParameters
        )
        val oldFinalPassword = computeFinalPasswordHash(
            basePassword = oldBasePassword.toHexHash(),
            email = email
        )

        // compute new password hashes
        val newBasePassword = computeBasePasswordHash(
            password = newPassword,
            email = email,
            parameters = argon2idParameters
        )
        val newFinalPassword = computeFinalPasswordHash(
            basePassword = newBasePassword.toHexHash(),
            email = email
        )

        // encrypt the new secrets
        val newSecrets = userSecrets.encrypt(newBasePassword)

        val request = ChangePasswordRequest(
            oldPassword = oldFinalPassword,
            newPassword = newFinalPassword,
            newEncryptionKey = newSecrets.encryptionKey,
            parallelism = argon2idParameters.parallelism,
            memory = argon2idParameters.memory,
            iterations = argon2idParameters.iterations,
            version = argon2idParameters.version
        )

        client.patch(
            "${API_ENDPOINT}/password",
            Json.encodeToString(ChangePasswordRequest.serializer(), request)
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
        val userSecrets = Json.decodeFromString(UserSecretsResponse.serializer(), response)
        return userSecrets.decrypt(basePassword)
    }
}
