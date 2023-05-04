package dev.medzik.librepass.client.api.v1

import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.api.v1.CipherClient.Companion.API_ENDPOINT
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.Cipher
import dev.medzik.librepass.types.api.EncryptedCipher
import dev.medzik.librepass.types.api.cipher.InsertResponse
import dev.medzik.librepass.types.api.cipher.SyncResponse
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.*

interface CipherClient {
    companion object {
        const val API_ENDPOINT = "/api/v1/cipher"
    }

    /**
     * Inserts a new cipher.
     * @param cipher The cipher to insert.
     * @param encryptionKey The encryption key to use for encrypting the cipher.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun insert(cipher: Cipher, encryptionKey: String): InsertResponse

    /**
     * Inserts a new cipher.
     * @param cipher The cipher to insert.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun insert(cipher: EncryptedCipher): InsertResponse

    /**
     * Gets a cipher.
     * @param id The UUID of the cipher.
     * @return [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: UUID): EncryptedCipher

    /**
     * Gets a cipher by its ID.
     * @param id The UUID of the cipher.
     * @return [EncryptedCipher]
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(id: String): EncryptedCipher

    /**
     * Get all ciphers.
     * @return List of ciphers.
     */
    @Throws(ClientException::class, ApiException::class)
    fun getAll(): List<EncryptedCipher>

    /**
     * Sync ciphers with the server.
     * @param lastSync The last sync date.
     * @return List of ciphers.
     */
    @Throws(ClientException::class, ApiException::class)
    fun sync(lastSync: Date): SyncResponse

    /**
     * Updates a cipher.
     * @param cipher The cipher to update.
     * @param encryptionKey The encryption key to use for encrypting the cipher.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun update(cipher: Cipher, encryptionKey: String): InsertResponse

    /**
     * Updates a cipher.
     * @param cipher The cipher to update.
     * @return [InsertResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun update(cipher: EncryptedCipher): InsertResponse

    /**
     * Deletes a cipher.
     * @param id The UUID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: UUID)

    /**
     * Deletes a cipher.
     * @param id The UUID of the cipher.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(id: String)
}

fun CipherClient(
    accessToken: String,
    apiUrl: String = Client.DefaultApiUrl
): CipherClient {
    return CipherClientImpl(accessToken, apiUrl)
}

class CipherClientImpl(
    accessToken: String,
    apiUrl: String
) : CipherClient {
    private val client = Client(accessToken, apiUrl)

    override fun insert(cipher: Cipher, encryptionKey: String): InsertResponse {
        return insert(cipher.toEncryptedCipher(encryptionKey))
    }

    override fun insert(cipher: EncryptedCipher): InsertResponse {
        val response = client.put(API_ENDPOINT, cipher.toJson())
        return Json.decodeFromString(InsertResponse.serializer(), response)
    }

    override fun get(id: UUID): EncryptedCipher {
        return get(id.toString())
    }

    override fun get(id: String): EncryptedCipher {
        val response = client.get("$API_ENDPOINT/$id")
        return Json.decodeFromString(EncryptedCipher.serializer(), response)
    }

    override fun getAll(): List<EncryptedCipher> {
        val response = client.get(API_ENDPOINT)
        return Json.decodeFromString(ListSerializer(EncryptedCipher.serializer()), response)
    }

    override fun sync(lastSync: Date): SyncResponse {
        val response = client.get("$API_ENDPOINT/sync/${lastSync.time / 1000}")
        return Json.decodeFromString(SyncResponse.serializer(), response)
    }

    override fun update(cipher: Cipher, encryptionKey: String): InsertResponse {
        return update(cipher.toEncryptedCipher(encryptionKey))
    }

    override fun update(cipher: EncryptedCipher): InsertResponse {
        val response = client.patch("$API_ENDPOINT/${cipher.id}", cipher.toJson())
        return Json.decodeFromString(InsertResponse.serializer(), response)
    }

    override fun delete(id: UUID) {
        delete(id.toString())
    }

    override fun delete(id: String) {
        client.delete("$API_ENDPOINT/$id")
    }
}
