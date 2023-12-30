package dev.medzik.librepass.types.cipher.data

import com.google.gson.annotations.JsonAdapter
import dev.medzik.librepass.types.adapters.DateAdapter
import java.util.*

/**
 * Cipher data for logins.
 *
 * @property name The name of the cipher.
 * @property username The login username.
 * @property password The login password.
 * @property passwordHistory The history of passwords.
 * @property uris The list of uris.
 * @property twoFactor The two-factor URI encoded.
 * @property notes The note for the cipher.
 * @property fields The custom fields for the cipher.
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
 * @property password The login password.
 * @property lastUsed The date when the password was changed.
 */
data class PasswordHistory(
    val password: String,
    @JsonAdapter(DateAdapter::class)
    val lastUsed: Date
)
