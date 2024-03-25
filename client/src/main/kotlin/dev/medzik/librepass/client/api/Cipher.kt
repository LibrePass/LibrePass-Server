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
 * Cipher Client for managing ciphers in the vault.
 *
 * @param apiKey user's api key
 * @param apiUrl server api url (default: official production server)
 */
class CipherClient(
    apiKey: String,
    apiUrl: String = Server.PRODUCTION
) {
    companion object {
        private const val API_ENDPOINT = "/api/cipher"

        /**
         * Gets favicon url of the domain.
         *
         * @param apiUrl server api url (default: official production server)
         * @param domain domain to get favicon from it
         * @return url address to the favicon
         */
        @Suppress("unused")
        fun getFavicon(
            apiUrl: String = Server.PRODUCTION,
            domain: String
        ) = "$apiUrl$API_ENDPOINT/icon?domain=$domain"
    }

    private val client = Client(apiUrl, apiKey)

    /**
     * Saves the cipher in the vault.
     *
     * @param cipher cipher to save
     * @param aesKey key for cipher encryption
     * @return [CipherIdResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun save(
        cipher: Cipher,
        aesKey: ByteArray
    ): CipherIdResponse {
        return save(
            EncryptedCipher(
                cipher = cipher,
                aesKey = aesKey
            )
        )
    }

    /**
     * Saves the cipher in the vault.
     *
     * @param cipher encrypted cipher to save
     * @return [CipherIdResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun save(cipher: EncryptedCipher): CipherIdResponse {
        val response = client.put(API_ENDPOINT, JsonUtils.serialize(cipher))
        return JsonUtils.deserialize(response)
    }

    /**
     * Retrieves cipher by its identifier.
     *
     * @param id cipher identifier
     * @return [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: UUID): EncryptedCipher {
        return get(id.toString())
    }

    /**
     * Retrieves cipher by its identifier.
     *
     * @param id cipher identifier
     * @return [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: String): EncryptedCipher {
        val response = client.get("$API_ENDPOINT/$id")
        return JsonUtils.deserialize(response)
    }

    /**
     * Gets all ciphers in the user vault.
     *
     * @return list of [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun getAll(): List<EncryptedCipher> {
        val response = client.get(API_ENDPOINT)
        return JsonUtils.deserialize(response)
    }

    /**
     * Synchronizes ciphers with the server.
     *
     * @param lastSync date of last successful synchronization
     * @return [SyncResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun sync(lastSync: Date): SyncResponse {
        val response = client.get("$API_ENDPOINT/sync?lastSync=${lastSync.time / 1000}")
        return JsonUtils.deserialize(response)
    }

    /**
     * Deletes cipher from the user vault.
     *
     * @param id cipher identifier
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: UUID) {
        delete(id.toString())
    }

    /**
     * Deletes cipher from the user vault.
     *
     * @param id cipher identifier
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: String) {
        client.delete("$API_ENDPOINT/$id")
    }
}
