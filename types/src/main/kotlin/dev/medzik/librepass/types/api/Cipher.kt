package dev.medzik.librepass.types.api

import com.google.gson.Gson
import dev.medzik.libcrypto.AesCbc
import java.util.*

/**
 * Cipher is a representation of a single cipher entry.
 * It contains all the information about the cipher.
 */
data class Cipher(
    val id: UUID,
    val owner: UUID,
    var type: Number,
    var data: CipherData,
    var favorite: Boolean = false,
    var collection: UUID? = null,
    var rePrompt: Boolean = false,
    var created: Date? = null,
    var lastModified: Date? = null
) {
    companion object {
        /**
         * Decrypts the [EncryptedCipher] and returns a new [Cipher] object.
         * @param cipher The [EncryptedCipher] to decrypt.
         * @param key The key to decrypt the cipher with.
         * @return The decrypted cipher.
         */
        fun from(cipher: EncryptedCipher, key: String): Cipher {
            return Cipher(
                id = cipher.id,
                owner = cipher.owner,
                type = cipher.type,
                data = cipher.decrypt(key),
                favorite = cipher.favorite,
                collection = cipher.collection,
                rePrompt = cipher.rePrompt,
                created = cipher.created,
                lastModified = cipher.lastModified
            )
        }
    }

    /**
     * Encrypts the cipher data and returns a new [EncryptedCipher] object.
     * @param key The key to encrypt the cipher with.
     * @return The encrypted cipher.
     */
    fun toEncryptedCipher(key: String) = EncryptedCipher.from(this, key)
}

/**
 * EncryptedCipher is a representation of a single cipher entry.
 * All sensitive data is encrypted.
 * This is the representation of the cipher that is stored in the server database.
 */
data class EncryptedCipher(
    val id: UUID,
    val owner: UUID,
    var type: Number = CipherType.Login.type,
    var data: String,
    var favorite: Boolean = false,
    var collection: UUID? = null,
    var rePrompt: Boolean = false,
    var created: Date? = null,
    var lastModified: Date? = null
) {
    companion object {
        /**
         * Encrypts the [Cipher] and returns a new [EncryptedCipher] object.
         * @param cipher The [Cipher] to encrypt.
         * @param key The key to encrypt the cipher with.
         * @return The encrypted cipher.
         */
        fun from(cipher: Cipher, key: String): EncryptedCipher {
            return EncryptedCipher(
                id = cipher.id,
                owner = cipher.owner,
                type = cipher.type,
                data = cipher.data.encrypt(key),
                favorite = cipher.favorite,
                collection = cipher.collection,
                rePrompt = cipher.rePrompt,
                created = cipher.created,
                lastModified = cipher.lastModified
            )
        }
    }

    /**
     * Decrypts the cipher data and returns a new [CipherData] object.
     * @param key The key to decrypt the cipher with.
     * @return The decrypted cipher data.
     */
    fun decrypt(key: String): CipherData {
        val data = AesCbc.decrypt(this.data, key)
        return Gson().fromJson(data, CipherData::class.java)
    }

    /**
     * Decrypts the cipher data and returns a new [Cipher] object.
     * @param key The key to decrypt the cipher with.
     * @return The decrypted cipher.
     */
    fun toCipher(key: String) = Cipher.from(this, key)

    /**
     * Converts the cipher to a JSON string.
     * @return The JSON string.
     */
    fun toJson(): String = Gson().toJson(this)
}

/**
 * CipherType is an enum that represents the type of the cipher.
 * It is used to indicate the type of the cipher.
 * It can be one of the following:
 * - Login: A login cipher.
 * - SecureNote: A secure note cipher.
 * - Card: A card cipher.
 * - Identity: An identity cipher.
 */
enum class CipherType(val type: Number) {
    Login(1),
    SecureNote(2),
    Card(3),
    Identity(4)
}

/**
 * CipherData is a representation of the data of a cipher.
 * In [EncryptedCipher] the data is encrypted.
 */
data class CipherData(
    var name: String,
    var username: String? = null,
    var password: String? = null,
    var uris: List<String>? = null,
    var twoFactor: String? = null,
    var notes: String? = null,
    var customFields: List<String>? = null
) {
    /**
     * Encrypts the cipher data and returns a new JSON string.
     * @param key The key to encrypt the cipher with.
     * @return The encrypted cipher data.
     */
    fun encrypt(key: String): String {
        val data = Gson().toJson(this)
        return AesCbc.encrypt(data, key)
    }
}
