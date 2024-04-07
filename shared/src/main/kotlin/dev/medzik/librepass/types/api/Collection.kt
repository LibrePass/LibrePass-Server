package dev.medzik.librepass.types.api

import com.google.gson.annotations.JsonAdapter
import dev.medzik.librepass.types.adapters.DateAdapter
import java.util.*

/**
 * Response from the API contains the Collection ID.
 */
data class CollectionIdResponse(
    val id: UUID
)

/**
 * Request for the collection endpoint, for creating a new collections.
 *
 * @property id The collection identifier.
 * @property name The name of the collection.
 */
data class CreateCollectionRequest(
    val id: UUID = UUID.randomUUID(),
    val name: String,
)

/**
 * Response from the API contains the collection data.
 *
 * @property id The collection identifier.
 * @property name The name of the collection.
 * @property owner The owner of the collection.
 * @property created The date when the collection was created.
 * @property lastModified The date when the collection was last modified.
 */
data class CipherCollection(
    val id: UUID,
    val name: String,
    val owner: UUID,

    @JsonAdapter(DateAdapter::class)
    val created: Date,
    @JsonAdapter(DateAdapter::class)
    val lastModified: Date
)
