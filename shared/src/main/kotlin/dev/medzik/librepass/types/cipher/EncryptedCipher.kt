package dev.medzik.librepass.types.cipher

import com.google.gson.annotations.JsonAdapter
import dev.medzik.librepass.types.adapters.DateAdapter
import dev.medzik.librepass.utils.EncryptedString
import dev.medzik.librepass.utils.decrypt
import java.util.*

/**
 * EncryptedCipher is a representation of cipher stored in the database.
 * The data is encrypted and can only be decrypted with the aes key.
 *
 * @property id The cipher identifier.
 * @property owner The owner identifier of the cipher.
 * @property type The type of the cipher.
 * @property protectedData The encrypted cipher data.
 * @property collection The identifier of the collection to which the cipher belongs.
 * @property favorite Whether the cipher is marked as favorite.
 * @property rePrompt Whether the password should be re-prompted. (Only UI-related)
 * @property created The date when the cipher was created.
 * @property lastModified The date when the cipher was last modified
 */
data class EncryptedCipher(
    val id: UUID,
    val owner: UUID,
    val type: Int = CipherType.Login.ordinal,
    val protectedData: EncryptedString,
    val collection: UUID? = null,
    val favorite: Boolean = false,
    val rePrompt: Boolean = false,
    // TODO: remove null after some time when users updates application
    @JsonAdapter(DateAdapter::class)
    val created: Date? = null,
    // TODO: the same as above
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
        protectedData = cipher.encryptData(aesKey),
        collection = cipher.collection,
        favorite = cipher.favorite,
        rePrompt = cipher.rePrompt,
        created = cipher.created,
        lastModified = cipher.lastModified
    )

    /** Decrypts the cipher data. */
    internal fun decryptData(aesKey: ByteArray): String = protectedData.decrypt(aesKey)
}
