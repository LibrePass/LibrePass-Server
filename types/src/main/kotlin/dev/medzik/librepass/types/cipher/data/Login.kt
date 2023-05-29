package dev.medzik.librepass.types.cipher.data

import dev.medzik.librepass.types.api.serializers.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

/**
 * CipherLoginData is a representation of the login data of a login cipher.
 * @property name The name of the login cipher.
 * @property username The username of the login cipher.
 * @property password The password of the login cipher.
 * @property passwordHistory The password history of the login cipher.
 * @property uris The list of URIs of the login cipher.
 * @property twoFactor The two-factor authentication code of the login cipher.
 * @property notes The notes of the login cipher.
 * @property fields The list of custom fields.
 */
@Serializable
data class CipherLoginData(
    val name: String,
    val username: String? = null,
    val password: String? = null,
    val passwordHistory: List<PasswordHistory>? = null,
    val uris: List<String>? = null,
    val twoFactor: String? = null,
    val notes: String? = null,
    val fields: List<CipherField>? = null
)

/**
 * PasswordHistory is a representation of the password history of a login cipher.
 * @param password The password of the login cipher.
 * @param lastUsed The date the password was last used.
 */
@Serializable
data class PasswordHistory(
    val password: String,
    @Serializable(with = DateSerializer::class)
    val lastUsed: Date
)
