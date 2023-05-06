@file:Suppress("unused")

package dev.medzik.librepass.types.api

import dev.medzik.libcrypto.AesCbc
import dev.medzik.librepass.types.api.serializers.DateSerializer
import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Cipher is a representation of a single cipher entry.
 * It contains all the information about the cipher.
 */
data class Cipher(
    val id: UUID,
    val owner: UUID,
    val type: Int,
    val data: CipherData,
    val favorite: Boolean = false,
    val collection: UUID? = null,
    val rePrompt: Boolean = false,
    val created: Date? = null,
    val lastModified: Date? = null
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
@Serializable
data class EncryptedCipher(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val owner: UUID,
    val type: Int = CipherType.Login.type,
    val data: String,
    val favorite: Boolean = false,
    @Serializable(with = UUIDSerializer::class)
    val collection: UUID? = null,
    val rePrompt: Boolean = false,
    @Serializable(with = DateSerializer::class)
    val created: Date? = null,
    @Serializable(with = DateSerializer::class)
    val lastModified: Date? = null
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
        return Json.decodeFromString(CipherData.serializer(), data)
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
    fun toJson(): String = Json.encodeToString(serializer(), this)
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
enum class CipherType(val type: Int) {
    Login(1),
    SecureNote(2),
    Card(3),
    Identity(4)
}

/**
 * CipherData is a representation of the data of a cipher.
 * In [EncryptedCipher] the data is encrypted.
 */
@Serializable
data class CipherData(
    val name: String,
    val username: String? = null,
    val password: String? = null,
    val uris: List<String>? = null,
    val twoFactor: String? = null,
    val notes: String? = null,
    val customFields: List<String>? = null
) {
    /**
     * Encrypts the cipher data and returns a new JSON string.
     * @param key The key to encrypt the cipher with.
     * @return The encrypted cipher data.
     */
    fun encrypt(key: String): String {
        val data = Json.encodeToString(serializer(), this)
        return AesCbc.encrypt(data, key)
    }
}
