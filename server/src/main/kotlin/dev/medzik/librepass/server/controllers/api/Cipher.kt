package dev.medzik.librepass.server.controllers.api

import dev.medzik.librepass.server.components.annotations.AuthorizedUser
import dev.medzik.librepass.server.controllers.advice.ServerException
import dev.medzik.librepass.server.database.CipherRepository
import dev.medzik.librepass.server.database.CipherTable
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.ratelimit.BaseRateLimitConfig
import dev.medzik.librepass.server.ratelimit.CipherControllerRateLimitConfig
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.server.utils.Validator
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
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/cipher")
class CipherController @Autowired constructor(
    private val cipherRepository: CipherRepository,
    @Value("\${server.api.rateLimit.enabled}")
    private val rateLimitEnabled: Boolean,
    @Value("\${limits.user.cipher}")
    private val userCiphersLimit: Long
) {
    private val rateLimit = CipherControllerRateLimitConfig()

    @PutMapping
    fun saveCipher(
        @AuthorizedUser user: UserTable,
        @Valid @RequestBody encryptedCipher: EncryptedCipher
    ): Response {
        consumeRateLimit(user.id.toString())

        if (
            // validates protected data
            !Validator.hexValidator(encryptedCipher.protectedData) ||
            // checks that the owner is correct
            encryptedCipher.owner != user.id ||
            // checks the user's cipher limit
            (
                !cipherRepository.existsByIdAndOwner(encryptedCipher.id, user.id) &&
                    cipherRepository.countByOwner(user.id) > userCiphersLimit
            )
        ) {
            throw ServerException.InvalidCipher("validation failed")
        }

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
        consumeRateLimit(user.id.toString())

        val ciphers = cipherRepository.getAllByOwner(user.id)

        // convert cipher table to encrypted ciphers
        val response = ciphers.map { it.toEncryptedCipher() }

        return ResponseHandler.generateResponse(response, HttpStatus.OK)
    }

    @GetMapping("/sync")
    fun syncCiphers(
        @AuthorizedUser user: UserTable,
        @RequestParam("lastSync") lastSyncUnixTimestamp: Long
    ): Response {
        consumeRateLimit(user.id.toString())

        val cipherIDs = cipherRepository.getAllIDs(user.id)

        val ciphers =
            cipherRepository.getAllByOwnerAndLastServerSync(
                user = user.id,
                date = Date(TimeUnit.SECONDS.toMillis(lastSyncUnixTimestamp))
            )

        val syncResponse =
            SyncResponse(
                // get ids of all ciphers
                ids = cipherIDs,
                // get all ciphers that were updated after timestamp
                ciphers =
                    ciphers
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
        consumeRateLimit(user.id.toString())

        if (!cipherRepository.existsByIdAndOwner(id, user.id))
            throw ServerException.CipherNotFound()

        val cipher = cipherRepository.findById(id).get()

        // convert to encrypted cipher
        val encryptedCipher = cipher.toEncryptedCipher()

        return ResponseHandler.generateResponse(encryptedCipher, HttpStatus.OK)
    }

    @PatchMapping("/{id}")
    @Deprecated("Use `/save` instead", ReplaceWith("saveCipher(user, encryptedCipher)"))
    fun updateCipher(
        @AuthorizedUser user: UserTable,
        @PathVariable id: UUID,
        @Valid @RequestBody encryptedCipher: EncryptedCipher
    ): Response {
        return saveCipher(user, encryptedCipher)
    }

    @DeleteMapping("/{id}")
    fun deleteCipher(
        @AuthorizedUser user: UserTable,
        @PathVariable id: UUID
    ): Response {
        consumeRateLimit(user.id.toString())

        if (!cipherRepository.existsByIdAndOwner(id, user.id))
            throw ServerException.CipherNotFound()

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
            throw ServerException.NotFound()
        }
    }

    private fun consumeRateLimit(
        key: String,
        rateLimitConfig: BaseRateLimitConfig = rateLimit
    ) {
        if (!rateLimitEnabled) return

        if (!rateLimitConfig.resolveBucket(key).tryConsume(1))
            throw ServerException.RateLimit()
    }
}
