package dev.medzik.librepass.types

import dev.medzik.libcrypto.AesCbc
import dev.medzik.librepass.types.api.serializers.DateSerializer
import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Cipher is a representation of a single cipher entry.
 * It contains all the information about the cipher.
 * @param id The unique identifier of the cipher.
 * @param owner The unique identifier of the owner of the cipher.
 * @param type The type of the cipher.
 * @param loginData The login data of the cipher. (Only if the cipher is a login cipher)
 * @param collection The unique identifier of the collection the cipher belongs to.
 * @param favorite Whether the cipher is marked as favorite.
 * @param rePrompt Whether the password should be re-prompted. (Only UI-related)
 * @param created The date the cipher was created.
 * @param lastModified The date the cipher was last modified.
 * @see LoginCipherData
 */
data class Cipher(
    val id: UUID,
    val owner: UUID,
    val type: CipherType,
    val loginData: LoginCipherData? = null,
    val collection: UUID? = null,
    val favorite: Boolean = false,
    val rePrompt: Boolean = false,
    val created: Date? = null,
    val lastModified: Date? = null
) {
    init {
        if (type == CipherType.Login && loginData == null) {
            throw IllegalArgumentException("Login cipher must have login data")
        }

        if (type != CipherType.Login && loginData != null) {
            throw IllegalArgumentException("Only login cipher can have login data")
        }
    }

    companion object {
        /**
         * Creates a new [Cipher] object from the [EncryptedCipher].
         * @param encryptedCipher The [EncryptedCipher] to decrypt.
         * @param encryptionKey The key to decrypt the cipher with.
         * @return The decrypted cipher.
         */
        fun from(encryptedCipher: EncryptedCipher, encryptionKey: String): Cipher {
            val type = CipherType.from(encryptedCipher.type)

            var loginData: LoginCipherData? = null

            // check if type is login
            if (type == CipherType.Login) {
                loginData = Json.decodeFromString(
                    LoginCipherData.serializer(),
                    encryptedCipher.decrypt(encryptionKey)
                )
            }

            return Cipher(
                id = encryptedCipher.id,
                owner = encryptedCipher.owner,
                type = type,
                loginData = loginData,
                collection = encryptedCipher.collection,
                favorite = encryptedCipher.favorite,
                rePrompt = encryptedCipher.rePrompt,
                created = encryptedCipher.created,
                lastModified = encryptedCipher.lastModified
            )
        }
    }

    /**
     * Converts the cipher to an [EncryptedCipher].
     * @param encryptionKey The key to encrypt the cipher with.
     * @return The encrypted cipher.
     */
    fun toEncryptedCipher(encryptionKey: String) =
        EncryptedCipher.from(this, encryptionKey)
}

/**
 * EncryptedCipher is a representation of cipher stored in the database.
 * The data is encrypted and can only be decrypted with the encryption key.
 * @param id The unique identifier of the cipher.
 * @param owner The unique identifier of the owner of the cipher.
 * @param type The type of the cipher.
 * @param data The encrypted data of the cipher.
 * @param collection The unique identifier of the collection the cipher belongs to.
 * @param favorite Whether the cipher is marked as favorite.
 * @param rePrompt Whether the password should be re-prompted. (Only UI-related)
 * @param created The date the cipher was created.
 * @param lastModified The date the cipher was last modified.
 * @see Cipher
 */
@Serializable
data class EncryptedCipher(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val owner: UUID,
    val type: Int = CipherType.Login.ordinal,
    val data: String,
    @Serializable(with = UUIDSerializer::class)
    val collection: UUID? = null,
    val favorite: Boolean = false,
    val rePrompt: Boolean = false,
    @Serializable(with = DateSerializer::class)
    val created: Date? = null,
    @Serializable(with = DateSerializer::class)
    val lastModified: Date? = null
) {
    companion object {
        /**
         * Creates a new [EncryptedCipher] object from the JSON string.
         * @param cipher The JSON string to decode.
         * @return The encrypted cipher.
         */
        fun from(cipher: String): EncryptedCipher =
            Json.decodeFromString(serializer(), cipher)

        /**
         * Creates a new [EncryptedCipher] object from the [Cipher].
         * @param cipher The [Cipher] to encrypt.
         * @param encryptionKey The key to encrypt the cipher with.
         * @return The encrypted cipher.
         */
        fun from(cipher: Cipher, encryptionKey: String): EncryptedCipher {
            val type = cipher.type.ordinal

            var data = ""

            // check if type is login
            if (type == CipherType.Login.ordinal) {
                data = Json.encodeToString(LoginCipherData.serializer(), cipher.loginData!!)
            }

            return EncryptedCipher(
                id = cipher.id,
                owner = cipher.owner,
                type = type,
                data = AesCbc.encrypt(data, encryptionKey),
                collection = cipher.collection,
                favorite = cipher.favorite,
                rePrompt = cipher.rePrompt,
                created = cipher.created,
                lastModified = cipher.lastModified
            )
        }
    }

    /**
     * Decrypts the cipher data.
     * @param encryptionKey The key to decrypt the cipher with.
     * @return JSON string of the decrypted cipher data.
     */
    fun decrypt(encryptionKey: String): String =
        AesCbc.decrypt(this.data, encryptionKey)

    /**
     * Converts the cipher to a JSON string.
     * @return JSON string of the cipher.
     */
    fun toJson(): String =
        Json.encodeToString(serializer(), this)
}

/**
 * CipherType is an enum class that represents the type of cipher.
 */
enum class CipherType {
    Login,
    SecureNote,
    Card,
    Identity;

    companion object {
        /**
         * Returns the [CipherType] from the given type integer.
         * @param type The type of the cipher.
         */
        fun from(type: Int) = values()[type]
    }
}

/**
 * LoginCipherData is a representation of the login data of a login cipher.
 * @param name The name of the login cipher.
 * @param username The username of the login cipher.
 * @param password The password of the login cipher.
 * @param uris The list of URIs of the login cipher.
 * @param twoFactor The two-factor authentication code of the login cipher.
 * @param notes The notes of the login cipher.
 * @param customFields The list of custom fields of the login cipher.
 */
@Serializable
data class LoginCipherData(
    val name: String,
    val username: String? = null,
    val password: String? = null,
    val uris: List<String>? = null,
    val twoFactor: String? = null,
    val notes: String? = null,
    val customFields: List<Map<String, String>>? = null
)
