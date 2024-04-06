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
import dev.medzik.librepass.types.api.SyncRequest
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

    /** Backward compatibility */
    @Deprecated("Use /sync endpoint")
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

    /** Backward compatibility */
    @Deprecated("Use /sync endpoint")
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

    /** Backward compatibility */
    @Deprecated("Use POST method")
    @GetMapping("/sync")
    fun syncCiphers(
        @AuthorizedUser user: UserTable,
        @RequestParam("lastSync") lastSyncUnixTimestamp: Long
    ): Response {
        val syncRequest = SyncRequest(
            lastSyncTimestamp = lastSyncUnixTimestamp,
            updated = emptyList(),
            deleted = emptyList()
        )

        return syncCiphers(user, syncRequest)
    }

    @PostMapping("/sync")
    fun syncCiphers(
        @AuthorizedUser user: UserTable,
        @RequestBody request: SyncRequest
    ): Response {
        consumeRateLimit(user.id.toString())

        var ownedCiphers = cipherRepository.countByOwner(user.id)
        val ownedCipherIDs = cipherRepository.getAllIDs(user.id)

        // valid all updated ciphers
        for (cipher in request.updated) {
            // check if protected data is hex
            if (!Validator.hexValidator(cipher.protectedData)) throw ServerException.InvalidCipher("validation failed")

            // check if the owner is correct
            if (cipher.owner != user.id) throw ServerException.InvalidCipher("validation failed")

            // check the user's cipher limit
            if (ownedCipherIDs.contains(cipher.id)) {
                ownedCiphers++

                if (ownedCiphers > userCiphersLimit) {
                    throw ServerException.InvalidCipher("current ciphers limit per user is $userCiphersLimit")
                }
            }
        }

        // delete ciphers from database
        cipherRepository.deleteAllByIdInAndOwner(request.deleted, user.id)

        // save ciphers into database
        if (request.updated.isNotEmpty()) {
            cipherRepository.saveAll(request.updated.map { CipherTable(it) } )
        }

        val updatedCiphers = cipherRepository.getAllByOwnerAndLastServerSync(
            user = user.id,
            date = Date(TimeUnit.SECONDS.toMillis(request.lastSyncTimestamp))
        )

        val syncResponse = SyncResponse(
            ids = cipherRepository.getAllIDs(user.id),
            ciphers = updatedCiphers.map { it.toEncryptedCipher() }
        )

        return ResponseHandler.generateResponse(syncResponse, HttpStatus.OK)
    }

    /** Backward compatibility */
    @Deprecated("Use /sync endpoint")
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

    /** Backward compatibility */
    @Deprecated("Use /sync endpoint")
    @PatchMapping("/{id}")
    fun updateCipher(
        @AuthorizedUser user: UserTable,
        @PathVariable id: UUID,
        @Valid @RequestBody encryptedCipher: EncryptedCipher
    ): Response {
        return saveCipher(user, encryptedCipher)
    }

    /** Backward compatibility */
    @Deprecated("Use /sync endpoint")
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
