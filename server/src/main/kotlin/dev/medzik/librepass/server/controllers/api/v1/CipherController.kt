package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.librepass.server.components.AuthorizedUser
import dev.medzik.librepass.server.database.CipherTable
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.services.CipherService
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseError
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.types.api.EncryptedCipher
import dev.medzik.librepass.types.api.cipher.InsertResponse
import dev.medzik.librepass.types.api.cipher.SyncResponse
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/cipher")
class CipherController {
    @Autowired
    private lateinit var cipherService: CipherService

    @PutMapping
    fun insertCipher(
        @AuthorizedUser user: UserTable?,
        @RequestBody encryptedCipher: EncryptedCipher
    ): Response {
        if (user == null) return ResponseError.Unauthorized

        return try {
            val cipher = cipherService.insertCipher(encryptedCipher)
            ResponseHandler.generateResponse(InsertResponse(cipher.id), HttpStatus.CREATED)
        } catch (e: Exception) {
            ResponseError.InvalidBody
        }
    }

    @GetMapping
    fun getAllCiphers(@AuthorizedUser user: UserTable?): Response {
        if (user == null) return ResponseError.Unauthorized
        val ciphers = cipherService.getAllCiphers(user.id)

        val json = Json.encodeToString(ListSerializer(CipherTable.serializer()), ciphers)
        return ResponseHandler.generateResponse(json, HttpStatus.OK)
    }

    @GetMapping("/sync")
    fun syncCiphers(
        @AuthorizedUser user: UserTable?,
        @RequestParam("lastSync") lastSyncUnixTimestamp: Long
    ): Response {
        if (user == null) return ResponseError.Unauthorized
        val ciphers = cipherService.sync(user.id, Date(lastSyncUnixTimestamp * 1000))

        val json = Json.encodeToString(SyncResponse.serializer(), ciphers)
        return ResponseHandler.generateResponse(json, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getCipher(
        @AuthorizedUser user: UserTable?,
        @PathVariable id: UUID
    ): Response {
        if (user == null) return ResponseError.Unauthorized
        val cipher = cipherService.getCipher(id, user.id) ?: return ResponseError.NotFound
        return ResponseHandler.generateResponse(Json.encodeToString(CipherTable.serializer(), cipher), HttpStatus.OK)
    }

    @PatchMapping("/{id}")
    fun updateCipher(
        @AuthorizedUser user: UserTable?,
        @PathVariable id: UUID,
        @RequestBody encryptedCipher: EncryptedCipher
    ): Response {
        if (user == null) return ResponseError.Unauthorized

        val cipher = cipherService.getCipher(id, user.id) ?: return ResponseError.NotFound
        if (cipher.id != encryptedCipher.id || cipher.owner != encryptedCipher.owner) return ResponseError.InvalidBody

        return try {
            cipherService.updateCipher(encryptedCipher.copy(
                created = cipher.created,
                lastModified = Date()
            ))
            ResponseHandler.generateResponse(InsertResponse(cipher.id), HttpStatus.OK)
        } catch (e: Exception) {
            ResponseError.InvalidBody
        }
    }

    @DeleteMapping("/{id}")
    fun deleteCipher(
        @AuthorizedUser user: UserTable?,
        @PathVariable id: UUID
    ): Response {
        if (user == null) return ResponseError.Unauthorized

        if (!cipherService.checkIfCipherExistsAndOwnedBy(id, user.id)) return ResponseError.NotFound

        cipherService.deleteCipher(id)
        return ResponseHandler.generateResponse(InsertResponse(id), HttpStatus.OK)
    }
}
