package dev.medzik.librepass.client.api.v1

import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.DEFAULT_API_URL
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.cipher.InsertResponse
import dev.medzik.librepass.types.api.collection.CipherCollection
import dev.medzik.librepass.types.api.collection.CreateCollectionRequest
import dev.medzik.librepass.types.utils.JsonUtils
import java.util.*

/**
 * Collection Client for the LibrePass API. This client is used to manage collections.
 * @param accessToken The access token to use.
 * @param apiUrl The API URL to use. Defaults to [DEFAULT_API_URL].
 */
class CollectionClient(
    accessToken: String,
    apiUrl: String = DEFAULT_API_URL
) {
    companion object {
        const val API_ENDPOINT = "/api/v1/collection"
    }

    private val client = Client(apiUrl, accessToken)

    /**
     * Create a new collection.
     * @param name The name of the collection.
     * @return The ID of the created collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun createCollection(name: String): InsertResponse {
        val request = CreateCollectionRequest(name = name)
        val response = client.put(API_ENDPOINT, JsonUtils.serialize(request))
        return JsonUtils.deserialize(response)
    }

    /**
     * Get all collections.
     * @return A list of all collections.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getCollections(): List<CipherCollection> {
        val response = client.get(API_ENDPOINT)
        return JsonUtils.deserializeList(response)
    }

    /**
     * Get a collection by ID.
     * @param id The ID of the collection.
     * @return The collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getCollection(id: UUID): CipherCollection {
        return getCollection(id.toString())
    }

    /**
     * Get a collection by ID.
     * @param id The ID of the collection.
     * @return The collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getCollection(id: String): CipherCollection {
        val response = client.get("$API_ENDPOINT/$id")
        return JsonUtils.deserialize(response)
    }

    /**
     * Update a collection.
     * @param id The ID of the collection.
     * @param name The new name of the collection.
     * @return The ID of the updated collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun updateCollection(id: UUID, name: String): InsertResponse {
        return updateCollection(id.toString(), name)
    }

    /**
     * Update a collection.
     * @param id The ID of the collection.
     * @param name The new name of the collection.
     * @return The ID of the updated collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun updateCollection(id: String, name: String): InsertResponse {
        val request = CreateCollectionRequest(name = name)
        val response = client.patch("$API_ENDPOINT/$id", JsonUtils.serialize(request))
        return JsonUtils.deserialize(response)
    }

    /**
     * Delete a collection.
     * @param id The ID of the collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun deleteCollection(id: UUID) {
        return deleteCollection(id.toString())
    }

    /**
     * Delete a collection.
     * @param id The ID of the collection.
     */
    @Throws(ClientException::class, ApiException::class)
    fun deleteCollection(id: String) {
        client.delete("$API_ENDPOINT/$id")
    }
}
