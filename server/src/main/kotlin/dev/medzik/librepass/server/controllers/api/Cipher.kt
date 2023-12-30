package dev.medzik.librepass.server.controllers.api

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.components.AuthorizedUser
import dev.medzik.librepass.server.database.CipherRepository
import dev.medzik.librepass.server.database.CipherTable
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.server.utils.Validator
import dev.medzik.librepass.server.utils.toResponse
import dev.medzik.librepass.types.api.CipherIdResponse
import dev.medzik.librepass.types.api.SyncResponse
import dev.medzik.librepass.types.cipher.EncryptedCipher
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.util.*

@RestController
@RequestMapping("/api/cipher")
class CipherController
    @Autowired
    constructor(
        private val cipherRepository: CipherRepository,
        @Value("\${limits.user.cipher}")
        private val userCiphersLimit: Long,
    ) {
        @PutMapping
        fun insertCipher(
            @AuthorizedUser user: UserTable,
            @Valid @RequestBody encryptedCipher: EncryptedCipher
        ): Response {
            if (
                // validates protected data
                !Validator.hexValidator(encryptedCipher.protectedData) ||
                // checks that the owner is correct
                encryptedCipher.owner != user.id ||
                // checks the user's cipher limit
                cipherRepository.countByOwner(user.id) > userCiphersLimit
            )
                return ResponseError.INVALID_BODY.toResponse()

            val cipher = cipherRepository.save(CipherTable(encryptedCipher))

            return ResponseHandler.generateResponse(
                CipherIdResponse(cipher.id),
                HttpStatus.CREATED
            )
        }

        @GetMapping
        fun getAllCiphers(
            @AuthorizedUser user: UserTable
        ): Response {
            val ciphers = cipherRepository.getAll(owner = user.id)

            // convert to encrypted ciphers
            val response = ciphers.map { it.toEncryptedCipher() }

            return ResponseHandler.generateResponse(response, HttpStatus.OK)
        }

        @GetMapping("/sync")
        fun syncCiphers(
            @AuthorizedUser user: UserTable,
            @RequestParam("lastSync") lastSyncUnixTimestamp: Long
        ): Response {
            // convert timestamp to date
            val lastSyncDate = Date(lastSyncUnixTimestamp * 1000)

            val ciphers = cipherRepository.getAll(owner = user.id)

            val syncResponse =
                SyncResponse(
                    // get ids of all ciphers
                    ids = ciphers.map { it.id },
                    // get all ciphers that were updated after timestamp
                    ciphers =
                        ciphers
                            // get all ciphers that were updated after timestamp
                            .filter { it.lastModified.after(lastSyncDate) }
                            // convert to encrypted ciphers
                            .map { it.toEncryptedCipher() }
                )

            return ResponseHandler.generateResponse(syncResponse, HttpStatus.OK)
        }

        @GetMapping("/{id}")
        fun getCipher(
            @AuthorizedUser user: UserTable,
            @PathVariable id: UUID
        ): Response {
            if (!cipherRepository.existsByIdAndOwner(id, user.id))
                return ResponseError.NOT_FOUND.toResponse()

            val cipher = cipherRepository.findById(id).get()

            // convert to encrypted cipher
            val encryptedCipher = cipher.toEncryptedCipher()

            return ResponseHandler.generateResponse(encryptedCipher, HttpStatus.OK)
        }

        @PatchMapping("/{id}")
        fun updateCipher(
            @AuthorizedUser user: UserTable,
            @PathVariable id: UUID,
            @Valid @RequestBody encryptedCipher: EncryptedCipher
        ): Response {
            if (!cipherRepository.existsByIdAndOwner(id, user.id))
                return ResponseError.NOT_FOUND.toResponse()

            if (!Validator.hexValidator(encryptedCipher.protectedData) ||
                encryptedCipher.owner != user.id
            )
                return ResponseError.INVALID_BODY.toResponse()

            cipherRepository.save(CipherTable(encryptedCipher))

            return ResponseHandler.generateResponse(CipherIdResponse(id), HttpStatus.OK)
        }

        @DeleteMapping("/{id}")
        fun deleteCipher(
            @AuthorizedUser user: UserTable,
            @PathVariable id: UUID
        ): Response {
            if (!cipherRepository.existsByIdAndOwner(id, user.id))
                return ResponseError.NOT_FOUND.toResponse()

            cipherRepository.deleteById(id)

            return ResponseHandler.generateResponse(CipherIdResponse(id), HttpStatus.OK)
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
                ResponseError.NOT_FOUND.toResponse()
            }
        }
    }
