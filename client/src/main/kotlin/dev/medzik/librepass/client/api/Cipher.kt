package dev.medzik.librepass.client.api

import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.CipherIdResponse
import dev.medzik.librepass.types.api.SyncResponse
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.EncryptedCipher
import java.util.*

/**
 * Cipher Client for the LibrePass API. This client is used to manage ciphers.
 * @param apiKey api key to use for authentication
 * @param apiUrl api url address (optional)
 */
class CipherClient(
    apiKey: String,
    apiUrl: String = Server.PRODUCTION
) {
    companion object {
        private const val API_ENDPOINT = "/api/cipher"

        /**
         * Get the favicon URL.
         *
         * @param apiUrl The LibrePass API url to use (default [Server.PRODUCTION])
         * @param domain The website domain.
         * @return URL to the favicon image.
         */
        fun getFavicon(
            apiUrl: String = Server.PRODUCTION,
            domain: String
        ) = "$apiUrl$API_ENDPOINT/icon?domain=$domain"
    }

    private val client = Client(apiUrl, apiKey)

    /**
     * Inserts a new cipher.
     *
     * @param cipher The cipher to insert.
     * @param aesKey The secret key to use for encryption.
     * @return ID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun insert(
        cipher: Cipher,
        aesKey: ByteArray
    ): CipherIdResponse {
        return insert(
            EncryptedCipher(
                cipher = cipher,
                aesKey = aesKey
            )
        )
    }

    /**
     * Inserts a new cipher.
     *
     * @param cipher The encrypted cipher to insert.
     * @return ID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun insert(cipher: EncryptedCipher): CipherIdResponse {
        val response = client.put(API_ENDPOINT, cipher.toJson())
        return JsonUtils.deserialize(response)
    }

    /**
     * Gets a cipher by its ID.
     *
     * @param id The cipher identifier.
     * @return ID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: UUID): EncryptedCipher {
        return get(id.toString())
    }

    /**
     * Gets a cipher by its ID.
     *
     * @param id The cipher identifier.
     * @return The encrypted cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: String): EncryptedCipher {
        val response = client.get("$API_ENDPOINT/$id")
        return JsonUtils.deserialize(response)
    }

    /**
     * Get all ciphers.
     *
     * @return List of encrypted ciphers.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getAll(): List<EncryptedCipher> {
        val response = client.get(API_ENDPOINT)
        return JsonUtils.deserialize(response)
    }

    /**
     * Sync ciphers with the server.
     *
     * @param lastSync The date of the last sync.
     * @return The sync response.
     */
    @Throws(ClientException::class, ApiException::class)
    fun sync(lastSync: Date): SyncResponse {
        val response = client.get("$API_ENDPOINT/sync?lastSync=${lastSync.time / 1000}")
        return JsonUtils.deserialize(response)
    }

    /**
     * Updates a cipher.
     *
     * @param cipher The cipher to update.
     * @param aesKey The secret key to use for encryption.
     * @return ID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun update(
        cipher: Cipher,
        aesKey: ByteArray
    ): CipherIdResponse {
        return update(
            EncryptedCipher(
                cipher = cipher,
                aesKey = aesKey
            )
        )
    }

    /**
     * Updates a cipher.
     *
     * @param cipher The cipher to update.
     * @return ID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun update(cipher: EncryptedCipher): CipherIdResponse {
        val response = client.patch("$API_ENDPOINT/${cipher.id}", cipher.toJson())
        return JsonUtils.deserialize(response)
    }

    /**
     * Deletes a cipher.
     *
     * @param id The cipher identifier.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: UUID) {
        delete(id.toString())
    }

    /**
     * Deletes a cipher.
     *
     * @param id The cipher identifier.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: String) {
        client.delete("$API_ENDPOINT/$id")
    }
}
