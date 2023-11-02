package dev.medzik.librepass.types.cipher.data

/**
 * CipherField represents a custom field in a cipher.
 *
 * @property name The name of the field.
 * @property type The type of the field.
 * @property value The field value.
 */
data class CipherField(
    val name: String,
    val type: CipherFieldType,
    val value: String
)

/** CipherFieldType is an enum class that represents the type of cipher field. */
enum class CipherFieldType {
    Text,

    @Suppress("unused")
    Hidden
}
