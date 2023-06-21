package dev.medzik.librepass.types.cipher

import com.google.gson.Gson
import com.google.gson.annotations.JsonAdapter
import dev.medzik.libcrypto.AES
import dev.medzik.librepass.types.adapters.DateAdapter
import java.util.*

/**
 * EncryptedCipher is a representation of cipher stored in the database.
 * The data is encrypted and can only be decrypted with the encryption key.
 * @property id cipher identifier
 * @property owner owner identifier
 * @property type type of the cipher
 * @property protectedData encrypted cipher data
 * @property collection unique identifier of the collection the cipher belongs to
 * @property favorite Whether the cipher is marked as favorite
 * @property rePrompt Whether the password should be re-prompted (Only UI-related)
 * @property version version of the cipher (the current version is 1)
 * @property created date the cipher was created
 * @property lastModified date the cipher was last modified
 * @see Cipher
 */
data class EncryptedCipher(
    val id: UUID,
    val owner: UUID,
    val type: Int = CipherType.Login.ordinal,
    val protectedData: String,
    val collection: UUID? = null,
    val favorite: Boolean = false,
    val rePrompt: Boolean = false,
    val version: Int = 1,
    @JsonAdapter(DateAdapter::class)
    val created: Date? = null,
    @JsonAdapter(DateAdapter::class)
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
        protectedData = AES.encrypt(
            AES.GCM,
            secretKey,
            Gson().toJson(
                when (cipher.type) {
                    CipherType.Login -> cipher.loginData
                    CipherType.Card -> cipher.cardData
                    CipherType.SecureNote -> cipher.secureNoteData
                }
            )!!
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
         */
        fun from(cipher: String): EncryptedCipher =
            Gson().fromJson(cipher, EncryptedCipher::class.java)
    }

    /**
     * Decrypts the cipher data.
     * @return JSON string of the decrypted cipher data.
     */
    fun decryptData(secretKey: String) =
        AES.decrypt(AES.GCM, secretKey, this.protectedData)!!

    /**
     * Converts the cipher to a JSON string.
     */
    fun toJson() =
        Gson().toJson(this)
}
