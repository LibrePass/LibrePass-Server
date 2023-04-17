package dev.medzik.librepass.client.api.v1

import com.google.gson.Gson
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.Cipher
import dev.medzik.librepass.types.api.EncryptedCipher
import dev.medzik.librepass.types.api.cipher.InsertResponse
import java.util.*

class CipherClient(accessToken: String, apiUrl: String = Client.DefaultApiUrl) {
    private val apiEndpoint = "/api/v1/cipher"

    private val client = Client(accessToken, apiUrl)

    /**
     * Inserts a new cipher.
     * @param cipher The cipher to insert.
     * @param encryptionKey The encryption key to use for encrypting the cipher.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun insert(cipher: Cipher, encryptionKey: String): InsertResponse {
        return insert(cipher.toEncryptedCipher(encryptionKey))
    }

    /**
     * Inserts a new cipher.
     * @param cipher The cipher to insert.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun insert(cipher: EncryptedCipher): InsertResponse {
        val response = client.put(apiEndpoint, cipher.toJson())
        return Gson().fromJson(response, InsertResponse::class.java)
    }

    /**
     * Gets a cipher.
     * @param id The UUID of the cipher.
     * @return [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: UUID): EncryptedCipher {
        return get(id.toString())
    }

    /**
     * Gets a cipher by its ID.
     * @param id The UUID of the cipher.
     * @return [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: String): EncryptedCipher {
        val response = client.get("$apiEndpoint/$id")
        return Gson().fromJson(response, EncryptedCipher::class.java)
    }

    /**
     * Get all cipher IDs.
     * @return List of cipher IDs
     */
    @Throws(ClientException::class, ApiException::class)
    fun getAll(): List<UUID> {
        val response = client.get(apiEndpoint)
        return Gson().fromJson(response, List::class.java).map { UUID.fromString(it.toString())}
    }

    /**
     * Updates a cipher.
     * @param cipher The cipher to update.
     * @param encryptionKey The encryption key to use for encrypting the cipher.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun update(cipher: Cipher, encryptionKey: String): InsertResponse {
        return update(cipher.toEncryptedCipher(encryptionKey))
    }

    /**
     * Updates a cipher.
     * @param cipher The cipher to update.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun update(cipher: EncryptedCipher): InsertResponse {
        val response = client.patch("$apiEndpoint/${cipher.id}", cipher.toJson())
        return Gson().fromJson(response, InsertResponse::class.java)
    }

    /**
     * Deletes a cipher.
     * @param id The UUID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: UUID) {
        delete(id.toString())
    }

    /**
     * Deletes a cipher.
     * @param id The UUID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: String) {
        client.delete("$apiEndpoint/$id")
    }
}
