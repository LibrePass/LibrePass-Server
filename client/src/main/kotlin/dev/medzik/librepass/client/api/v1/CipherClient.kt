package dev.medzik.librepass.client.api.v1

import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.DEFAULT_API_URL
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.cipher.InsertResponse
import dev.medzik.librepass.types.api.cipher.SyncResponse
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.EncryptedCipher
import dev.medzik.librepass.types.utils.JsonUtils
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.*

/**
 * Cipher API client.
 * @param accessToken The access token to use for authentication.
 * @param apiUrl The API URL to use. Defaults to [DEFAULT_API_URL].
 */
@Suppress("unused")
class CipherClient(
    accessToken: String,
    apiUrl: String  = DEFAULT_API_URL
) {
    companion object {
        const val API_ENDPOINT = "/api/v1/cipher"

        /**
         * Get website favicon (Using Cloudflare Workers).
         * Always using default API URL (https://librepass.favicon.workers.dev)
         * because this is not a part of server but Cloudflare Workers.
         */
        @JvmStatic
        @Throws(ClientException::class)
        fun getFavicon(url: String): ByteArray {
            try {
                // send request
                val response = OkHttpClient().newCall(
                    okhttp3.Request.Builder()
                        .url("${DEFAULT_API_URL}/_cf/favicon/?url=$url")
                        .get()
                        .build()
                ).execute()

                // extract from response
                val statusCode = response.code
                val body = response.body.bytes()

                // error handling
                if (statusCode >= 300) {
                    throw ApiException(
                        status = statusCode,
                        error = "Error while getting favicon: $body"
                    )
                }

                return body
            } catch (e: IOException) {
                throw ClientException(e)
            }
        }
    }

    private val client = Client(apiUrl, accessToken)

    /**
     * Inserts a new cipher.
     * @param cipher The cipher to insert.
     * @param encryptionKey The encryption key to use for encrypting the cipher.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun insert(cipher: Cipher, encryptionKey: String): InsertResponse {
        return insert(
            EncryptedCipher(
                cipher = cipher,
                encryptionKey = encryptionKey
            )
        )
    }

    /**
     * Inserts a new cipher.
     * @param cipher The cipher to insert.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun insert(cipher: EncryptedCipher): InsertResponse {
        val response = client.put(API_ENDPOINT, cipher.toJson())
        return JsonUtils.deserialize(response)
    }

    /**
     * Gets a cipher.
     * @param id The UUID of the cipher.
     * @return [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: UUID): EncryptedCipher {
        return get(id.toString())
    }

    /**
     * Gets a cipher by its ID.
     * @param id The UUID of the cipher.
     * @return [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: String): EncryptedCipher {
        val response = client.get("$API_ENDPOINT/$id")
        return JsonUtils.deserialize(response)
    }

    /**
     * Get all ciphers.
     * @return List of ciphers.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getAll(): List<EncryptedCipher> {
        val response = client.get(API_ENDPOINT)
        return JsonUtils.deserializeList(response)
    }

    /**
     * Sync ciphers with the server.
     * @param lastSync The last sync date.
     * @return List of ciphers.
     */
    @Throws(ClientException::class, ApiException::class)
    fun sync(lastSync: Date): SyncResponse {
        val response = client.get("$API_ENDPOINT/sync?lastSync=${lastSync.time / 1000}")
        return JsonUtils.deserialize(response)
    }

    /**
     * Updates a cipher.
     * @param cipher The cipher to update.
     * @param encryptionKey The encryption key to use for encrypting the cipher.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun update(cipher: Cipher, encryptionKey: String): InsertResponse {
        return update(
            EncryptedCipher(
                cipher = cipher,
                encryptionKey = encryptionKey
            )
        )
    }

    /**
     * Updates a cipher.
     * @param cipher The cipher to update.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun update(cipher: EncryptedCipher): InsertResponse {
        val response = client.patch("$API_ENDPOINT/${cipher.id}", cipher.toJson())
        return JsonUtils.deserialize(response)
    }

    /**
     * Deletes a cipher.
     * @param id The UUID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: UUID) {
        delete(id.toString())
    }

    /**
     * Deletes a cipher.
     * @param id The UUID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: String) {
        client.delete("$API_ENDPOINT/$id")
    }
}
