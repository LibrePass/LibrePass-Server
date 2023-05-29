package dev.medzik.librepass.types

import dev.medzik.libcrypto.AesCbc
import dev.medzik.librepass.types.api.serializers.DateSerializer
import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Cipher is a representation of a single cipher entry.
 * It contains all the information about the cipher.
 * @property id The unique identifier of the cipher.
 * @property owner The unique identifier of the owner of the cipher.
 * @property type The type of the cipher.
 * @property loginData The login data of the cipher. (Only if the cipher is a login cipher)
 * @property collection The unique identifier of the collection the cipher belongs to.
 * @property favorite Whether the cipher is marked as favorite.
 * @property rePrompt Whether the password should be re-prompted. (Only UI-related)
 * @property version The version of the cipher. (Currently 1)
 * @property created The date the cipher was created.
 * @property lastModified The date the cipher was last modified.
 * @see LoginCipherData
 * @see SecureNoteCipherData
 * @see CardCipherData
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
    val version: Int = 1,
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

    /**
     * Creates a new [Cipher] object from the [EncryptedCipher].
     * @param encryptedCipher The [EncryptedCipher] to decrypt.
     * @param encryptionKey The key to decrypt the cipher with.
     * @return The decrypted cipher.
     */
    constructor(
        encryptedCipher: EncryptedCipher,
        encryptionKey: String
    ) : this(
        id = encryptedCipher.id,
        owner = encryptedCipher.owner,
        type = CipherType.from(encryptedCipher.type),
        loginData = if (encryptedCipher.type == CipherType.Login.ordinal) {
            Json.decodeFromString(
                LoginCipherData.serializer(),
                encryptedCipher.decrypt(encryptionKey)
            )
        } else null,
        secureNoteData = if (encryptedCipher.type == CipherType.SecureNote.ordinal) {
            Json.decodeFromString(
                SecureNoteCipherData.serializer(),
                encryptedCipher.decrypt(encryptionKey)
            )
        } else null,
        cardData = if (encryptedCipher.type == CipherType.Card.ordinal) {
            Json.decodeFromString(
                CardCipherData.serializer(),
                encryptedCipher.decrypt(encryptionKey)
            )
        } else null,
        collection = encryptedCipher.collection,
        favorite = encryptedCipher.favorite,
        rePrompt = encryptedCipher.rePrompt,
        version = encryptedCipher.version,
        created = encryptedCipher.created,
        lastModified = encryptedCipher.lastModified
    )
}

/**
 * EncryptedCipher is a representation of cipher stored in the database.
 * The data is encrypted and can only be decrypted with the encryption key.
 * @property id The unique identifier of the cipher.
 * @property owner The unique identifier of the owner of the cipher.
 * @property type The type of the cipher.
 * @property data The encrypted data of the cipher.
 * @property collection The unique identifier of the collection the cipher belongs to.
 * @property favorite Whether the cipher is marked as favorite.
 * @property rePrompt Whether the password should be re-prompted. (Only UI-related)
 * @property version The version of the cipher. (Currently 1)
 * @property created The date the cipher was created.
 * @property lastModified The date the cipher was last modified.
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
    val version: Int = 1,
    @Serializable(with = DateSerializer::class)
    val created: Date? = null,
    @Serializable(with = DateSerializer::class)
    val lastModified: Date? = null
) {
    /**
     * Creates a new [EncryptedCipher] object from the [Cipher].
     * @param cipher The [Cipher] to encrypt.
     * @param encryptionKey The key to encrypt the cipher with.
     * @return The encrypted cipher.
     */
    constructor(
        cipher: Cipher,
        encryptionKey: String
    ) : this(
        id = cipher.id,
        owner = cipher.owner,
        type = cipher.type.ordinal,
        data = AesCbc.encrypt(
            when (cipher.type) {
                CipherType.Login -> Json.encodeToString(LoginCipherData.serializer(), cipher.loginData!!)
                CipherType.SecureNote -> Json.encodeToString(SecureNoteCipherData.serializer(), cipher.secureNoteData!!)
                CipherType.Card -> Json.encodeToString(CardCipherData.serializer(), cipher.cardData!!)
            },
            encryptionKey
        ),
        collection = cipher.collection,
        favorite = cipher.favorite,
        rePrompt = cipher.rePrompt,
        version = cipher.version,
        created = cipher.created,
        lastModified = cipher.lastModified
    )

    companion object {
        /**
         * Creates a new [EncryptedCipher] object from the JSON string.
         * @param cipher The JSON string to decode.
         * @return The encrypted cipher.
         */
        fun from(cipher: String) =
            Json.decodeFromString(serializer(), cipher)
    }

    /**
     * Decrypts the cipher data.
     * @param encryptionKey The key to decrypt the cipher with.
     * @return JSON string of the decrypted cipher data.
     */
    fun decrypt(encryptionKey: String) =
        AesCbc.decrypt(this.data, encryptionKey)!!

    /**
     * Converts the cipher to a JSON string.
     * @return JSON string of the cipher.
     */
    fun toJson() =
        Json.encodeToString(serializer(), this)
}

/**
 * CipherType is an enum class that represents the type of cipher.
 */
enum class CipherType {
    Login,
    SecureNote,
    Card;

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
 * @property name The name of the login cipher.
 * @property username The username of the login cipher.
 * @property password The password of the login cipher.
 * @property uris The list of URIs of the login cipher.
 * @property twoFactor The two-factor authentication code of the login cipher.
 * @property notes The notes of the login cipher.
 * @property fields The list of custom fields.
 */
@Serializable
data class LoginCipherData(
    val name: String,
    val username: String? = null,
    val password: String? = null,
    val uris: List<String>? = null,
    val twoFactor: String? = null,
    val notes: String? = null,
    val fields: List<CipherField>? = null
)

/**
 * SecureNoteCipherData is a representation of the note data of a secure note cipher.
 * @param title The title of the secure note cipher.
 * @param note The note of the secure note cipher.
 * @param fields The list of custom fields.
 */
@Serializable
data class SecureNoteCipherData(
    val title: String,
    val note: String,
    val fields: List<CipherField>? = null
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
 * @param fields The list of custom fields.
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
    val fields: List<CipherField>? = null
)

/**
 * CipherField is a representation of a custom field of a cipher.
 * @property name The name of the custom field.
 * @property type The type of the custom field.
 * @property value The value of the custom field.
 * @see CipherFieldType
 */
@Serializable
data class CipherField(
    val name: String,
    val type: CipherFieldType,
    val value: String
)

/**
 * CipherFieldType is an enum class that represents the type of cipher field.
 */
@Serializable(with = CipherFieldTypeSerializer::class)
enum class CipherFieldType {
    Text,
    Hidden
}

/**
 * Serializer for [CipherFieldType] enum class. Serializes to and from [Int].
 */
private object CipherFieldTypeSerializer : KSerializer<CipherFieldType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CipherFieldType", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: CipherFieldType) =
        encoder.encodeInt(value.ordinal)

    override fun deserialize(decoder: Decoder): CipherFieldType =
        CipherFieldType.values()[decoder.decodeInt()]
}
