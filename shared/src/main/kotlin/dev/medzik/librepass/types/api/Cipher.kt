package dev.medzik.librepass.types.api

import dev.medzik.librepass.types.cipher.EncryptedCipher
import java.util.*

/**
 * Response from the API contains the Cipher ID.\
 */
data class CipherIdResponse(
    val id: UUID
)

/**
 * Request for the sync request.
 *
 * @param lastSyncTimestamp unix timestamp (seconds) of the last sync
 * @param updated new or updated ciphers to save into the server database
 * @param deleted IDs with deleted ciphers to delete it from the server database
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
 * @property ciphers List of ciphers updated after a given date or all ciphers.
 *  (Depending on the request sent, whether it contains lastSync on timestamp or whether on zero)
 */
data class SyncResponse(
    val ids: List<UUID>,
    val ciphers: List<EncryptedCipher>
)
