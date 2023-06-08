package dev.medzik.librepass.types.cipher.data

import kotlinx.serialization.Serializable

/**
 * Card data for cipher.
 * @param cardholderName cardholder name
 * @param brand card brand
 * @param number card number
 * @param expMonth card expiration month
 * @param expYear card expiration year
 * @param code CVV card code
 * @param notes notes for card
 * @param fields custom fields
 */
@Serializable
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
