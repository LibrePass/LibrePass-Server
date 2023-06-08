package dev.medzik.librepass.types.cipher

import dev.medzik.libcrypto.AES
import dev.medzik.librepass.types.api.serializers.DateSerializer
import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import dev.medzik.librepass.types.cipher.data.CipherCardData
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import dev.medzik.librepass.types.cipher.data.CipherSecureNoteData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

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
     * @param secretKey The key to encrypt the cipher with.
     * @return The encrypted cipher.
     */
    constructor(
        cipher: Cipher,
        secretKey: String
    ) : this(
        id = cipher.id,
        owner = cipher.owner,
        type = cipher.type.ordinal,
        data = AES.encrypt(
            AES.GCM,
            secretKey,
            when (cipher.type) {
                CipherType.Login -> Json.encodeToString(CipherLoginData.serializer(), cipher.loginData!!)
                CipherType.SecureNote -> Json.encodeToString(CipherSecureNoteData.serializer(), cipher.secureNoteData!!)
                CipherType.Card -> Json.encodeToString(CipherCardData.serializer(), cipher.cardData!!)
            }
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
     * @param secretKey The key to decrypt the cipher with.
     * @return JSON string of the decrypted cipher data.
     */
    fun decryptData(secretKey: String) =
        AES.decrypt(AES.GCM, secretKey, this.data)!!

    /**
     * Converts the cipher to a JSON string.
     * @return JSON string of the cipher.
     */
    fun toJson() =
        Json.encodeToString(serializer(), this)
}
