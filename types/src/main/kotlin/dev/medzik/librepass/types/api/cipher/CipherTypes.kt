package dev.medzik.librepass.types.api.cipher

import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import dev.medzik.librepass.types.cipher.EncryptedCipher
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Response for insert request.
 * @property id ID of the inserted cipher.
 */
@Serializable
data class InsertResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)

/**
 * Response for sync request.
 * @property ids List of IDs of all ciphers. Used for checking if cipher was deleted, it will not be in this list.
 * @property ciphers List of all ciphers that were updated after timestamp.
 */
@Serializable
data class SyncResponse(
    val ids: List<@Serializable(with = UUIDSerializer::class) UUID>,
    val ciphers: List<EncryptedCipher>
)
