package dev.medzik.librepass.types.cipher

import com.google.gson.Gson
import dev.medzik.librepass.types.cipher.data.CipherCardData
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import dev.medzik.librepass.types.cipher.data.CipherSecureNoteData
import dev.medzik.librepass.utils.EncryptedString
import dev.medzik.librepass.utils.encrypt
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Cipher is a representation of a single cipher entry.
 *
 * @property id The cipher identifier.
 * @property owner The identifier of the cipher owner.
 * @property type The type of the cipher.
 * @property loginData The login data. (Only if the cipher is a login cipher)
 * @property secureNoteData The secure note data. (Only if the cipher is a secure cipher)
 * @property cardData The card data. (Only if the cipher is a card cipher)
 * @property collection The identifier of collection,
 * @property favorite Whether the cipher is a favorite cipher.
 * @property rePrompt Whether the cipher should be re-prompted. (Only UI feature)
 * @property created The date when the cipher was created.
 * @property lastModified The date when the cipher was last modified.
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
    val created: Date = currentFixedDate(),
    val lastModified: Date = currentFixedDate()
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

    /** Creates a new [Cipher] object from the [EncryptedCipher]. */
    constructor(
        encryptedCipher: EncryptedCipher,
        aesKey: ByteArray
    ) : this(
        id = encryptedCipher.id,
        owner = encryptedCipher.owner,
        type = CipherType.from(encryptedCipher.type),
        loginData = decryptData(CipherType.Login, encryptedCipher, aesKey),
        secureNoteData = decryptData(CipherType.SecureNote, encryptedCipher, aesKey),
        cardData = decryptData(CipherType.Card, encryptedCipher, aesKey),
        collection = encryptedCipher.collection,
        favorite = encryptedCipher.favorite,
        rePrompt = encryptedCipher.rePrompt,
        // TODO: remove null after some time when users updates application
        created = encryptedCipher.created ?: currentFixedDate(),
        // TODO: the same as above
        lastModified = encryptedCipher.lastModified ?: currentFixedDate()
    )

    /** Encrypts the cipher data. */
    fun encryptData(aesKey: ByteArray): EncryptedString {
        val cipherText =
            Gson().toJson(
                when (type) {
                    CipherType.Login -> loginData
                    CipherType.Card -> cardData
                    CipherType.SecureNote -> secureNoteData
                }
            )

        return cipherText.encrypt(aesKey)
    }

    /**
     * Clones the cipher and sets the [lastModified] property to the current time.
     *
     * @return A new [Cipher] object with the same properties as the original, except for the [lastModified] property.
     */
    fun withUpdatedLastModified(): Cipher {
        return copy(lastModified = currentFixedDate())
    }

    companion object {
        private fun currentUnixSeconds(): Long = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

        fun currentFixedDate(): Date = Date(TimeUnit.SECONDS.toMillis(currentUnixSeconds()))

        /** Decrypts the data of the [EncryptedCipher] if the type matches. */
        private inline fun <reified T> decryptData(
            type: CipherType,
            encryptedCipher: EncryptedCipher,
            aesKey: ByteArray
        ): T? =
            if (type.ordinal == encryptedCipher.type) {
                Gson().fromJson(encryptedCipher.decryptData(aesKey), T::class.java)
            } else {
                null
            }
    }
}
