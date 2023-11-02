package dev.medzik.librepass.types.cipher.data

/**
 * Card data for cipher.
 *
 * @property cardholderName cardholder name
 * @property brand card brand
 * @property number card number
 * @property expMonth card expiration month
 * @property expYear card expiration year
 * @property code CVV card code
 * @property notes notes for card
 * @property fields custom fields
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
