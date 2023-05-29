package dev.medzik.librepass.types.cipher.data

import kotlinx.serialization.Serializable

/**
 * CipherCardData is a representation of the card data of a card cipher.
 * @param cardholderName The cardholder name of the card cipher.
 * @param brand The brand of the card cipher.
 * @param number The number of the card cipher.
 * @param expMonth The expiration month of the card cipher.
 * @param expYear The expiration year of the card cipher.
 * @param code The code of the card cipher.
 * @param notes The notes of the card cipher.
 * @param fields The list of custom fields.
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
