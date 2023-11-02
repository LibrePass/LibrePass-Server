package dev.medzik.librepass.types.cipher.data

/**
 * Cipher data for cards.
 *
 * @property cardholderName The cardholder name.
 * @property brand The brand of the card.
 * @property number The number of the card.
 * @property expMonth The card expiration month.
 * @property expYear The card expiration year.
 * @property code The card CVV code.
 * @property notes The note for the cipher.
 * @property fields The custom fields for the cipher.
 */
data class CipherCardData(
    val cardholderName: String,
    val brand: String? = null,
    val number: String? = null,
    val expMonth: Int? = null,
    val expYear: Int? = null,
    val code: String? = null,
    val notes: String? = null,
    val fields: List<CipherField>? = null
)
