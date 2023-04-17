package dev.medzik.librepass.client.api.v1

import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.Cipher
import dev.medzik.librepass.types.api.EncryptedCipher
import dev.medzik.librepass.types.api.cipher.InsertResponse
import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
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
        return Json.decodeFromString(InsertResponse.serializer(), response)
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
        return Json.decodeFromString(EncryptedCipher.serializer(), response)
    }

    /**
     * Get all cipher IDs.
     * @return List of cipher IDs
     */
    @Throws(ClientException::class, ApiException::class)
    fun getAll(): List<UUID> {
        val response = client.get(apiEndpoint)
        return Json.decodeFromString(ListSerializer(UUIDSerializer), response)
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
        return Json.decodeFromString(InsertResponse.serializer(), response)
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
