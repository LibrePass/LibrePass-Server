package dev.medzik.librepass.types.cipher.data

/**
 * Secure note for the cipher.
 *
 * @param title title of the note
 * @param note secure note
 * @param fields custom fields
 */
data class CipherSecureNoteData(
    val title: String,
    val note: String,
    val fields: List<CipherField>? = null
)
