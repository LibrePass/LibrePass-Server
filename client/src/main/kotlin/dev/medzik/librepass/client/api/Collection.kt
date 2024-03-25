package dev.medzik.librepass.client.api

import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.CipherCollection
import dev.medzik.librepass.types.api.CollectionIdResponse
import dev.medzik.librepass.types.api.CreateCollectionRequest
import java.util.*

/**
 * Collection Client for managing collections in the vault.
 *
 * @param apiKey user's api key
 * @param apiUrl server api url (default: official production server)
 */
class CollectionClient(
    apiKey: String,
    apiUrl: String = Server.PRODUCTION
) {
    companion object {
        private const val API_ENDPOINT = "/api/collection"
    }

    private val client = Client(apiUrl, apiKey)

    /**
     * Insert collection.
     *
     * @param name The collection name.
     * @return ID of the cipher collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun save(name: String): CollectionIdResponse {
        val request = CreateCollectionRequest(name = name)
        val response = client.put(API_ENDPOINT, JsonUtils.serialize(request))
        return JsonUtils.deserialize(response)
    }

    /**
     * Update collection.
     *
     * @param id The collection id.
     * @param name The collection name.
     * @return ID of the cipher collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun save(
        id: UUID,
        name: String
    ): CollectionIdResponse {
        val request =
            CreateCollectionRequest(
                id = id,
                name = name
            )
        val response = client.put(API_ENDPOINT, JsonUtils.serialize(request))
        return JsonUtils.deserialize(response)
    }

    /**
     * Get all collections.
     *
     * @return List of cipher collections.
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(): List<CipherCollection> {
        val response = client.get(API_ENDPOINT)
        return JsonUtils.deserialize(response)
    }

    /**
     * Get collection by ID.
     *
     * @param id The collection identifier.
     * @return The cipher collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: UUID): CipherCollection {
        return get(id.toString())
    }

    /**
     * Get collection by ID.
     *
     * @param id collection identifier
     * @return The cipher collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: String): CipherCollection {
        val response = client.get("$API_ENDPOINT/$id")
        return JsonUtils.deserialize(response)
    }

    /**
     * Delete collection.
     *
     * @param id The collection identifier.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: UUID) {
        return delete(id.toString())
    }

    /**
     * Delete collection.
     *
     * @param id The collection identifier.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: String) {
        client.delete("$API_ENDPOINT/$id")
    }
}
