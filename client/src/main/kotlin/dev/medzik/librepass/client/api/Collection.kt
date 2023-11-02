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
 * Collection Client for managing collections.
 *
 * @param apiKey The API key to use for authentication.
 * @param apiUrl The API url address (default official production server)
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
     * Creates a new collection.
     *
     * @param name The collection name.
     * @return ID of the cipher collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun createCollection(name: String): CollectionIdResponse {
        val request = CreateCollectionRequest(name = name)
        val response = client.put(API_ENDPOINT, JsonUtils.serialize(request))
        return JsonUtils.deserialize(response)
    }

    /**
     * Gets all collections.
     *
     * @return List of cipher collections.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getCollections(): List<CipherCollection> {
        val response = client.get(API_ENDPOINT)
        return JsonUtils.deserialize(response)
    }

    /**
     * Gets a collection by ID.
     *
     * @param id The collection identifier.
     * @return The cipher collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getCollection(id: UUID): CipherCollection {
        return getCollection(id.toString())
    }

    /**
     * Gets a collection by ID.
     *
     * @param id collection identifier
     * @return The cipher collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getCollection(id: String): CipherCollection {
        val response = client.get("$API_ENDPOINT/$id")
        return JsonUtils.deserialize(response)
    }

    /**
     * Updates a collection
     *
     * @param id The collection identifier.
     * @param name The collection name.
     * @return ID of the cipher collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun updateCollection(
        id: UUID,
        name: String
    ): CollectionIdResponse {
        return updateCollection(id.toString(), name)
    }

    /**
     * Updates a collection.
     *
     * @param id The collection identifier.
     * @param name The collection name.
     * @return ID of the cipher collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun updateCollection(
        id: String,
        name: String
    ): CollectionIdResponse {
        val request = CreateCollectionRequest(name = name)
        val response = client.patch("$API_ENDPOINT/$id", JsonUtils.serialize(request))
        return JsonUtils.deserialize(response)
    }

    /**
     * Deletes a collection.
     *
     * @param id The collection identifier.
     */
    @Throws(ClientException::class, ApiException::class)
    fun deleteCollection(id: UUID) {
        return deleteCollection(id.toString())
    }

    /**
     * Deletes a collection.
     *
     * @param id The collection identifier.
     */
    @Throws(ClientException::class, ApiException::class)
    fun deleteCollection(id: String) {
        client.delete("$API_ENDPOINT/$id")
    }
}
