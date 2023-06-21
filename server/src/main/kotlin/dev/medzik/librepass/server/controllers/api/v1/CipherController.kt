package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.librepass.server.components.AuthorizedUser
import dev.medzik.librepass.server.database.CipherRepository
import dev.medzik.librepass.server.database.CipherTable
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseError
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.types.api.cipher.InsertResponse
import dev.medzik.librepass.types.api.cipher.SyncResponse
import dev.medzik.librepass.types.cipher.EncryptedCipher
import kotlinx.serialization.builtins.ListSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.util.*

@RestController
@RequestMapping("/api/v1/cipher")
class CipherController @Autowired constructor(
    private val cipherRepository: CipherRepository
) {
    @PutMapping
    fun insertCipher(
        @AuthorizedUser user: UserTable?,
        @RequestBody encryptedCipher: EncryptedCipher
    ): Response {
        if (user == null)
            return ResponseError.Unauthorized

        return try {
            val cipher = cipherRepository.save(CipherTable(encryptedCipher))

            ResponseHandler.generateResponse(
                data = InsertResponse(cipher.id),
                status = HttpStatus.CREATED
            )
        } catch (e: Exception) {
            ResponseError.InvalidBody
        }
    }

    @GetMapping
    fun getAllCiphers(@AuthorizedUser user: UserTable?): Response {
        if (user == null)
            return ResponseError.Unauthorized

        // get all ciphers owned by user
        val ciphers = cipherRepository.getAll(owner = user.id)

        return ResponseHandler.generateResponse(
            serializer = ListSerializer(EncryptedCipher.serializer()),
            // convert to encrypted ciphers
            data = ciphers.map { it.toEncryptedCipher() },
            status = HttpStatus.OK
        )
    }

    @GetMapping("/sync")
    fun syncCiphers(
        @AuthorizedUser user: UserTable?,
        @RequestParam("lastSync") lastSyncUnixTimestamp: Long
    ): Response {
        if (user == null)
            return ResponseError.Unauthorized

        // convert timestamp to date
        val lastSyncDate = Date(lastSyncUnixTimestamp * 1000)

        // get all ciphers owned by user
        val ciphers = cipherRepository.getAll(owner = user.id)

        // prepare response
        val syncResponse = SyncResponse(
            // get ids of all ciphers
            ids = ciphers.map { it.id },
            // get all ciphers that were updated after timestamp
            ciphers = ciphers
                // get all ciphers that were updated after timestamp
                .filter { it.lastModified.after(lastSyncDate) }
                // convert to encrypted ciphers
                .map { it.toEncryptedCipher() }
        )

        return ResponseHandler.generateResponse(syncResponse, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getCipher(
        @AuthorizedUser user: UserTable?,
        @PathVariable id: UUID
    ): Response {
        if (user == null)
            return ResponseError.Unauthorized

        // get cipher by id
        val cipher = cipherRepository.findById(id).orElse(null)
            ?: return ResponseError.NotFound

        // check if cipher is owned by user (if not, return 404)
        if (cipher.owner != user.id)
            return ResponseError.NotFound

        // convert to encrypted cipher
        val encryptedCipher = cipher.toEncryptedCipher()

        return ResponseHandler.generateResponse(encryptedCipher, HttpStatus.OK)
    }

    @PatchMapping("/{id}")
    fun updateCipher(
        @AuthorizedUser user: UserTable?,
        @PathVariable id: UUID,
        @RequestBody encryptedCipher: EncryptedCipher
    ): Response {
        if (user == null)
            return ResponseError.Unauthorized

        // get cipher table from encrypted cipher
        val cipher = CipherTable(encryptedCipher)

        // check if cipher exists and is owned by user (if not, return 404)
        if (!checkIfCipherExistsAndOwnedBy(id, user.id))
            return ResponseError.NotFound

        // update cipher
        cipherRepository.save(cipher)

        // prepare response
        val response = InsertResponse(cipher.id)

        return ResponseHandler.generateResponse(response, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteCipher(
        @AuthorizedUser user: UserTable?,
        @PathVariable id: UUID
    ): Response {
        if (user == null)
            return ResponseError.Unauthorized

        // check if cipher exists and is owned by user (if not, return 404)
        if (!checkIfCipherExistsAndOwnedBy(id, user.id))
            return ResponseError.NotFound

        // delete cipher
        cipherRepository.deleteById(id)

        // prepare response
        val response = InsertResponse(id)

        return ResponseHandler.generateResponse(response, HttpStatus.OK)
    }

    /**
     * Checks if cipher exists and is owned by user.
     */
    private fun checkIfCipherExistsAndOwnedBy(id: UUID, owner: UUID): Boolean {
        return cipherRepository.checkIfCipherExistsAndOwnedBy(id, owner)
    }

    @GetMapping("/icon")
    fun getWebsiteIcon(
        @RequestParam("domain") domain: String
    ): Any {
        // Some APIs to get website icon:
        //  google api: https://www.google.com/s2/favicons?domain=$domain&sz=128
        //  duckduckgo api: https://icons.duckduckgo.com/ip3/$domain.ico
        //  icon.horse: https://icon.horse/icon/$domain

        // get using google api
        val uri = "https://www.google.com/s2/favicons?domain=$domain&sz=128"

        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.IMAGE_PNG)

        val entity = HttpEntity<String>(headers)

        return try {
            restTemplate.exchange(uri, HttpMethod.GET, entity, ByteArray::class.java)
        } catch (e: Exception) {
            ResponseError.NotFound
        }
    }
}
