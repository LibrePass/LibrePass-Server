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
     * Creates a new collection.
     *
     * @param name collection name
     * @return [CollectionIdResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun save(name: String): CollectionIdResponse {
        val request = CreateCollectionRequest(name = name)
        val response = client.put(API_ENDPOINT, JsonUtils.serialize(request))
        return JsonUtils.deserialize(response)
    }

    /**
     * Updates a collection.
     *
     * @param id collection identifier
     * @param name collection name
     * @return [CollectionIdResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun save(
        id: UUID,
        name: String
    ): CollectionIdResponse {
        val request = CreateCollectionRequest(
            id = id,
            name = name
        )
        val response = client.put(API_ENDPOINT, JsonUtils.serialize(request))
        return JsonUtils.deserialize(response)
    }

    /**
     * Gets all collections.
     *
     * @return list of [CipherCollection]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(): List<CipherCollection> {
        val response = client.get(API_ENDPOINT)
        return JsonUtils.deserialize(response)
    }

    /**
     * Gets a collection.
     *
     * @param id collection identifier
     * @return [CipherCollection]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: UUID): CipherCollection {
        return get(id.toString())
    }

    /**
     * Gets a collection.
     *
     * @param id collection identifier
     * @return [CipherCollection]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: String): CipherCollection {
        val response = client.get("$API_ENDPOINT/$id")
        return JsonUtils.deserialize(response)
    }

    /**
     * Deletes collection.
     *
     * @param id collection identifier
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: UUID) {
        return delete(id.toString())
    }

    /**
     * Deletes collection.
     *
     * @param id collection identifier
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: String) {
        client.delete("$API_ENDPOINT/$id")
    }
}
