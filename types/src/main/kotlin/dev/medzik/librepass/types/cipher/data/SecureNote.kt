package dev.medzik.librepass.types.cipher.data

import kotlinx.serialization.Serializable

/**
 * CipherSecureNoteData is a representation of the note data of a secure note cipher.
 * @param title The title of the secure note cipher.
 * @param note The note of the secure note cipher.
 * @param fields The list of custom fields.
 */
@Serializable
data class CipherSecureNoteData(
    val title: String,
    val note: String,
    val fields: List<CipherField>? = null
)
