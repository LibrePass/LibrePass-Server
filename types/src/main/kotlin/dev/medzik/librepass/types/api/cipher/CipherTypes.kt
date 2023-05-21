package dev.medzik.librepass.types.api.cipher

import dev.medzik.librepass.types.EncryptedCipher
import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Response for insert request.
 */
@Serializable
data class InsertResponse(
    /**
     * ID of the inserted cipher.
     */
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)

/**
 * Response for sync request.
 */
@Serializable
data class SyncResponse(
    /**
     * List of IDs of all ciphers.
     *
     * Used for checking if cipher was deleted, it will not be in this list.
     */
    val ids: List<@Serializable(with = UUIDSerializer::class) UUID>,
    /**
     * List of all ciphers that were updated after timestamp.
     */
    val ciphers: List<EncryptedCipher>
)
