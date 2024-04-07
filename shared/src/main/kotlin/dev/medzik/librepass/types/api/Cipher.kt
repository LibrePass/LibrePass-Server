package dev.medzik.librepass.types.api

import dev.medzik.librepass.types.cipher.EncryptedCipher
import java.util.*

/**
 * Response from the API contains the Cipher ID.
 */
@Deprecated("Moved all endpoints to sync endpoint")
data class CipherIdResponse(
    val id: UUID
)

/**
 * Request for the sync endpoint.
 *
 * @param lastSyncTimestamp The unix timestamp (seconds) of the last synchronization.
 * @param updated The list of new or updated ciphers to save into the server database.
 * @param deleted The list of identifiers with deleted ciphers to delete it from the server database.
 */
data class SyncRequest(
    val lastSyncTimestamp: Long,
    val updated: List<EncryptedCipher>,
    val deleted: List<UUID>
)

/**
 * Response from the sync request contains ciphers.
 *
 * @property ids The list of all cipher IDs owned by user.
 * @property ciphers The new or updated ciphers updated in the server database since the last synchronization.
 */
data class SyncResponse(
    val ids: List<UUID>,
    val ciphers: List<EncryptedCipher>
)
