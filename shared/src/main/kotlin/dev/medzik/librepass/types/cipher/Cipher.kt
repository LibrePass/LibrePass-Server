package dev.medzik.librepass.types.cipher

import com.google.gson.Gson
import dev.medzik.librepass.types.cipher.data.CipherCardData
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import dev.medzik.librepass.types.cipher.data.CipherSecureNoteData
import java.util.*

/**
 * Cipher is a representation of a single cipher entry.
 * @property id cipher identifier
 * @property owner owner identifier
 * @property type cipher type - [CipherType]
 * @property loginData login data (Only if the cipher is a login cipher) - [CipherLoginData]
 * @property secureNoteData secure note data (Only if the cipher is a secure cipher) - [CipherSecureNoteData]
 * @property cardData card data (Only if the cipher is a card cipher) - [CipherCardData]
 * @property collection collection identifier
 * @property favorite whether the cipher is a favorite cipher
 * @property rePrompt whether the cipher should be re-prompted (Only UI feature)
 * @property version cipher version (the current version is 1)
 * @property created created date
 * @property lastModified last modified date
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
     */
    constructor(
        encryptedCipher: EncryptedCipher,
        secretKey: String
    ) : this(
        id = encryptedCipher.id,
        owner = encryptedCipher.owner,
        type = CipherType.from(encryptedCipher.type),
        loginData = decryptData(CipherType.Login, encryptedCipher, secretKey),
        secureNoteData = decryptData(CipherType.SecureNote, encryptedCipher, secretKey),
        cardData = decryptData(CipherType.Card, encryptedCipher, secretKey),
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
         * @param type type of the cipher
         * @param encryptedCipher encrypted cipher to decrypt
         * @param secretKey secret key to use for decrypting
         * @return The decrypted data or null if the type doesn't match.
         */
        private inline fun <reified T> decryptData(
            type: CipherType,
            encryptedCipher: EncryptedCipher,
            secretKey: String
        ): T? =
            if (type.ordinal == encryptedCipher.type)
                Gson().fromJson(encryptedCipher.decryptData(secretKey), T::class.java)
            else
                null
    }
}
