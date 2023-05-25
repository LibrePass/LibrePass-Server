package dev.medzik.librepass.types.api.collection

import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Request for creating new collection.
 * @property id ID of the collection. If not provided, random UUID will be generated.
 * @property name Name of the collection.
 */
@Serializable
data class CreateCollectionRequest(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val name: String,
)

/**
 * Response of cipher collection.
 * @property id ID of the collection.
 * @property name Name of the collection.
 * @property owner ID of the owner of the collection.
 */
@Serializable
data class CipherCollection(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    @Serializable(with = UUIDSerializer::class)
    val owner: UUID
)
