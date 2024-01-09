package dev.medzik.librepass.types.cipher

import com.google.gson.Gson
import com.google.gson.annotations.JsonAdapter
import dev.medzik.libcrypto.Aes
import dev.medzik.librepass.types.adapters.DateAdapter
import java.util.*

/**
 * EncryptedCipher is a representation of cipher stored in the database.
 * The data is encrypted and can only be decrypted with the encryption key.
 *
 * @property id The cipher identifier.
 * @property owner The owner identifier of the cipher.
 * @property type The type of the cipher.
 * @property protectedData The encrypted cipher data.
 * @property collection The identifier of the collection to which the cipher belongs.
 * @property favorite Whether the cipher is marked as favorite.
 * @property rePrompt Whether the password should be re-prompted. (Only UI-related)
 * @property version The version of the cipher. (the current version is 1)
 * @property created The date when the cipher was created.
 * @property lastModified The date when the cipher was last modified
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
     *
     * @param cipher The [Cipher] to encrypt.
     * @param aesKey The key to use for encryption.
     * @return The encrypted cipher.
     */
    constructor(
        cipher: Cipher,
        aesKey: ByteArray
    ) : this(
        id = cipher.id,
        owner = cipher.owner,
        type = cipher.type.ordinal,
        protectedData =
            Aes.encrypt(
                Aes.GCM,
                aesKey,
                Gson().toJson(
                    when (cipher.type) {
                        CipherType.Login -> cipher.loginData
                        CipherType.Card -> cipher.cardData
                        CipherType.SecureNote -> cipher.secureNoteData
                    },
                ).toByteArray()
            ),
        collection = cipher.collection,
        favorite = cipher.favorite,
        rePrompt = cipher.rePrompt,
        version = cipher.version,
        created = cipher.created,
        lastModified = cipher.lastModified
    )

    companion object {
        /** Creates a new [EncryptedCipher] object from the JSON string. */
        @JvmStatic
        fun from(cipher: String): EncryptedCipher = Gson().fromJson(cipher, EncryptedCipher::class.java)
    }

    /** Decrypts the cipher data. */
    fun decryptData(aesKey: ByteArray): String {
        return String(Aes.decrypt(Aes.GCM, aesKey, this.protectedData))
    }

    /** Converts the cipher to a JSON string. */
    fun toJson(): String = Gson().toJson(this)
}
