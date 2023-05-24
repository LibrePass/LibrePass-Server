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
    val secureNoteData: SecureNoteCipherData? = null,
    val cardData: CardCipherData? = null,
    val collection: UUID? = null,
    val favorite: Boolean = false,
    val rePrompt: Boolean = false,
    val created: Date? = null,
    val lastModified: Date? = null
) {
    init {
        if (type == CipherType.Login && loginData == null)
            throw IllegalArgumentException("Login cipher must have login data")

        if (type != CipherType.Login && loginData != null)
            throw IllegalArgumentException("Only login cipher can have login data")

        if (type == CipherType.SecureNote && secureNoteData == null)
            throw IllegalArgumentException("Secure note cipher must have secure note data")

        if (type != CipherType.SecureNote && secureNoteData != null)
            throw IllegalArgumentException("Only secure note cipher can have secure note data")

        if (type == CipherType.Card && cardData == null)
            throw IllegalArgumentException("Card cipher must have card data")

        if (type != CipherType.Card && cardData != null)
            throw IllegalArgumentException("Only card cipher can have card data")
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
            var secureNoteData: SecureNoteCipherData? = null
            var cardData: CardCipherData? = null

            // check if type is login
            when (type) {
                CipherType.Login -> {
                    loginData = Json.decodeFromString(
                        LoginCipherData.serializer(),
                        encryptedCipher.decrypt(encryptionKey)
                    )
                }
                CipherType.SecureNote -> {
                    secureNoteData = Json.decodeFromString(
                        SecureNoteCipherData.serializer(),
                        encryptedCipher.decrypt(encryptionKey)
                    )
                }
                CipherType.Card -> {
                    cardData = Json.decodeFromString(
                        CardCipherData.serializer(),
                        encryptedCipher.decrypt(encryptionKey)
                    )
                }
                CipherType.Identity -> throw IllegalArgumentException("Identity cipher is not supported yet")
            }

            return Cipher(
                id = encryptedCipher.id,
                owner = encryptedCipher.owner,
                type = type,
                loginData = loginData,
                secureNoteData = secureNoteData,
                cardData = cardData,
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

            when (type) {
                CipherType.Login.ordinal -> {
                    data = Json.encodeToString(LoginCipherData.serializer(), cipher.loginData!!)
                }
                CipherType.SecureNote.ordinal -> {
                    data = Json.encodeToString(SecureNoteCipherData.serializer(), cipher.secureNoteData!!)
                }
                CipherType.Card.ordinal -> {
                    data = Json.encodeToString(CardCipherData.serializer(), cipher.cardData!!)
                }
                CipherType.Identity.ordinal ->
                    throw IllegalArgumentException("Identity cipher is not supported yet")
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

/**
 * SecureNoteCipherData is a representation of the note data of a secure note cipher.
 * @param title The title of the secure note cipher.
 * @param note The note of the secure note cipher.
 */
@Serializable
data class SecureNoteCipherData(
    val title: String,
    val note: String
)

/**
 * CardCipherData is a representation of the card data of a card cipher.
 * @param cardholderName The cardholder name of the card cipher.
 * @param brand The brand of the card cipher.
 * @param number The number of the card cipher.
 * @param expMonth The expiration month of the card cipher.
 * @param expYear The expiration year of the card cipher.
 * @param code The code of the card cipher.
 * @param notes The notes of the card cipher.
 * @param customFields The list of custom fields of the card cipher.
 */
@Serializable
data class CardCipherData(
    val cardholderName: String,
    val brand: String? = null,
    val number: String? = null,
    val expMonth: Int? = null,
    val expYear: Int? = null,
    val code: String? = null,
    val notes: String? = null,
    val customFields: List<Map<String, String>>? = null
)

// TODO: Implement IdentityCipherData
