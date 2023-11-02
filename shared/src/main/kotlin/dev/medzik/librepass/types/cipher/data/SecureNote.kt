package dev.medzik.librepass.types.cipher.data

/**
 * Cipher data for secure notes.
 *
 * @param title The title of the note.
 * @param note The secure note.
 * @property fields The custom fields for the cipher.
 */
data class CipherSecureNoteData(
    val title: String,
    val note: String,
    val fields: List<CipherField>? = null
)
