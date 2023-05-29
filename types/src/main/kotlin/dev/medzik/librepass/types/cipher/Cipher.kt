package dev.medzik.librepass.types.cipher

import dev.medzik.librepass.types.cipher.data.CipherCardData
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import dev.medzik.librepass.types.cipher.data.CipherSecureNoteData
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
 * @see CipherLoginData
 * @see CipherSecureNoteData
 * @see CipherCardData
 */
data class Cipher(
    val id: UUID,
    val owner: UUID,
    val type: CipherType,
    val loginData: CipherLoginData? = null,
    val secureNoteData: CipherSecureNoteData? = null,
    val cardData: CipherCardData? = null,
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
        loginData = decryptData(CipherType.Login, encryptedCipher, encryptionKey),
        secureNoteData = decryptData(CipherType.SecureNote, encryptedCipher, encryptionKey),
        cardData = decryptData(CipherType.Card, encryptedCipher, encryptionKey),
        collection = encryptedCipher.collection,
        favorite = encryptedCipher.favorite,
        rePrompt = encryptedCipher.rePrompt,
        version = encryptedCipher.version,
        created = encryptedCipher.created,
        lastModified = encryptedCipher.lastModified
    )

    companion object {
        /**
         * Decrypts the data of the [EncryptedCipher] if the type matches.
         * @param type The type of the cipher.
         * @param encryptedCipher The [EncryptedCipher] to decrypt.
         * @param encryptionKey The key to decrypt the cipher with.
         * @return The decrypted data or null if the type doesn't match.
         */
        private inline fun <reified T> decryptData(
            type: CipherType,
            encryptedCipher: EncryptedCipher,
            encryptionKey: String
        ): T? =
            if (type.ordinal == encryptedCipher.type)
                Json.decodeFromString(encryptedCipher.decryptData(encryptionKey))
            else null
    }
}
