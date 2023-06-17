package dev.medzik.librepass.client.api.v1

import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.DEFAULT_API_URL
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.cipher.InsertResponse
import dev.medzik.librepass.types.api.cipher.SyncResponse
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.EncryptedCipher
import java.util.*

/**
 * Cipher Client for the LibrePass API. This client is used to manage ciphers.
 * @param apiKey api key to use for authentication
 * @param apiUrl api url address (optional)
 */
@Suppress("unused")
class CipherClient(
    apiKey: String,
    apiUrl: String  = DEFAULT_API_URL
) {
    companion object {
        private const val API_ENDPOINT = "/api/v1/cipher"
    }

    private val client = Client(apiUrl, apiKey)

    /**
     * Inserts a new cipher.
     * @param cipher cipher to insert
     * @param secretKey secret key to use for encrypting the cipher
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun insert(cipher: Cipher, secretKey: String): InsertResponse {
        return insert(
            EncryptedCipher(
                cipher = cipher,
                secretKey = secretKey
            )
        )
    }

    /**
     * Inserts a new cipher.
     * @param cipher encrypted cipher to insert
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun insert(cipher: EncryptedCipher): InsertResponse {
        val response = client.put(API_ENDPOINT, cipher.toJson())
        return JsonUtils.deserialize(response)
    }

    /**
     * Gets a cipher by its ID.
     * @param id cipher identifier
     * @return [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: UUID): EncryptedCipher {
        return get(id.toString())
    }

    /**
     * Gets a cipher by its ID.
     * @param id cipher identifier
     * @return [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: String): EncryptedCipher {
        val response = client.get("$API_ENDPOINT/$id")
        return JsonUtils.deserialize(response)
    }

    /**
     * Get all ciphers.
     * @return List of [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun getAll(): List<EncryptedCipher> {
        val response = client.get(API_ENDPOINT)
        return JsonUtils.deserializeList(response)
    }

    /**
     * Sync ciphers with the server.
     * @param lastSync date of the last sync
     * @return [SyncResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun sync(lastSync: Date): SyncResponse {
        val response = client.get("$API_ENDPOINT/sync?lastSync=${lastSync.time / 1000}")
        return JsonUtils.deserialize(response)
    }

    /**
     * Updates a cipher.
     * @param cipher cipher to update
     * @param secretKey secret key to use for encrypting the cipher
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun update(cipher: Cipher, secretKey: String): InsertResponse {
        return update(
            EncryptedCipher(
                cipher = cipher,
                secretKey = secretKey
            )
        )
    }

    /**
     * Updates a cipher.
     * @param cipher cipher to update
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun update(cipher: EncryptedCipher): InsertResponse {
        val response = client.patch("$API_ENDPOINT/${cipher.id}", cipher.toJson())
        return JsonUtils.deserialize(response)
    }

    /**
     * Deletes a cipher.
     * @param id cipher identifier
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: UUID) {
        delete(id.toString())
    }

    /**
     * Deletes a cipher.
     * @param id cipher identifier
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: String) {
        client.delete("$API_ENDPOINT/$id")
    }

    /**
     * Get website favicon.
     * @param domain website domain
     * @return Favicon image as byte array (PNG) or 404 if not found.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getFavicon(domain: String): ByteArray {
        val response = client.get("$API_ENDPOINT/icon?domain=$domain")
        return response.toByteArray()
    }
}
