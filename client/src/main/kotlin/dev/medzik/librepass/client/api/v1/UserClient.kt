package dev.medzik.librepass.client.api.v1

import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import dev.medzik.librepass.types.api.user.ChangePasswordRequest
import dev.medzik.librepass.types.api.user.UserSecretsResponse
import kotlinx.serialization.json.Json

interface UserClient {
    companion object {
        const val API_ENDPOINT = "/api/v1/user"
    }

    /**
     * Change user password.
     * @param oldPassword Old password.
     * @param newPassword New password.
     */
    @Throws(ClientException::class, ApiException::class)
    fun changePassword(oldPassword: String, newPassword: String, parameters: UserArgon2idParameters? = null)

    /**
     * Get user secrets.
     * @param password User password.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getSecrets(password: String): UserSecretsResponse

    /**
     * Get user secrets.
     * @param basePassword User base password hash.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getSecrets(basePassword: Argon2Hash): UserSecretsResponse
}

fun UserClient(
    email: String,
    accessToken: String,
    apiUrl: String = Client.DefaultApiUrl
): UserClient {
    return UserClientImpl(email, accessToken, apiUrl)
}

class UserClientImpl(
    private val email: String,
    accessToken: String,
    private val apiUrl: String
): UserClient {
    private val client = Client(accessToken, apiUrl)

    override fun changePassword(oldPassword: String, newPassword: String, parameters: UserArgon2idParameters?) {
        val userSecrets = getSecrets(oldPassword)

        val argon2idParameters = AuthClient(apiUrl = apiUrl).getUserArgon2idParameters(email)

        val oldBasePassword = AuthClient.computeBasePasswordHash(
            password = oldPassword,
            email = email,
            parameters = argon2idParameters
        )
        val oldFinalPassword = AuthClient.computeFinalPasswordHash(
            basePassword = oldBasePassword.toHexHash(),
            email = email
        )

        val newBasePassword = AuthClient.computeBasePasswordHash(
            password = newPassword,
            email = email,
            parameters = argon2idParameters
        )
        val newFinalPassword = AuthClient.computeFinalPasswordHash(
            basePassword = newBasePassword.toHexHash(),
            email = email
        )

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
            "${UserClient.API_ENDPOINT}/password",
            Json.encodeToString(ChangePasswordRequest.serializer(), request)
        )
    }

    override fun getSecrets(password: String): UserSecretsResponse {
        val argon2idParameters = AuthClient(apiUrl = apiUrl).getUserArgon2idParameters(email)

        // compute base password
        val basePassword = AuthClient.computeBasePasswordHash(
            password = password,
            email = email,
            parameters = argon2idParameters
        )

        return getSecrets(basePassword)
    }

    override fun getSecrets(basePassword: Argon2Hash): UserSecretsResponse {
        val response = client.get("${UserClient.API_ENDPOINT}/secrets")
        val userSecrets = Json.decodeFromString(UserSecretsResponse.serializer(), response)
        return userSecrets.decrypt(basePassword)
    }
}
