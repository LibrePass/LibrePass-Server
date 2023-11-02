package dev.medzik.librepass.types.cipher.data

import com.google.gson.annotations.JsonAdapter
import dev.medzik.librepass.types.adapters.DateAdapter
import java.util.*

/**
 * Login data for cipher.
 *
 * @property name cipher name
 * @property username login username
 * @property password login password
 * @property passwordHistory password history
 * @property uris list of URIs
 * @property twoFactor two-factor authentication secret
 * @property notes notes for the cipher
 * @property fields custom fields
 */
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
 * Passwords history in [CipherLoginData].
 *
 * @property password login password
 * @property lastUsed date when the password was changed
 */
data class PasswordHistory(
    val password: String,
    @JsonAdapter(DateAdapter::class)
    val lastUsed: Date
)
