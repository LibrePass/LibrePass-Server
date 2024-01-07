package dev.medzik.librepass.types.cipher.data

/**
 * Cipher data for cards.
 *
 * @property name The name of the cipher.
 * @property cardholderName The cardholder name.
 * @property number The number of the card.
 * @property expMonth The card expiration month.
 * @property expYear The card expiration year.
 * @property code The card CVV code.
 * @property notes The note for the cipher.
 * @property fields The custom fields for the cipher.
 */
data class CipherCardData(
    val name: String,
    val cardholderName: String,
    val number: String,
    val expMonth: String? = null,
    val expYear: String? = null,
    val code: String? = null,
    val notes: String? = null,
    val fields: List<CipherField>? = null
)
