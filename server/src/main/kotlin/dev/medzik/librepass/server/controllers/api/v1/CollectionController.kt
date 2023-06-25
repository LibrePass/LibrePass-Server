package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.components.AuthorizedUser
import dev.medzik.librepass.server.database.CollectionRepository
import dev.medzik.librepass.server.database.CollectionTable
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.server.utils.toResponse
import dev.medzik.librepass.types.api.cipher.InsertResponse
import dev.medzik.librepass.types.api.collection.CipherCollection
import dev.medzik.librepass.types.api.collection.CreateCollectionRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/collection")
class CollectionController @Autowired constructor(
    private val collectionRepository: CollectionRepository
) {
    @PutMapping
    fun insertCollection(
        @AuthorizedUser user: UserTable,
        @RequestBody collection: CreateCollectionRequest
    ): Response {
        collectionRepository.save(
            CollectionTable(
                id = collection.id,
                name = collection.name,
                owner = user.id
            )
        )

        // prepare response
        val response = InsertResponse(collection.id)

        return ResponseHandler.generateResponse(response, HttpStatus.CREATED)
    }

    @GetMapping
    fun getAllCollections(@AuthorizedUser user: UserTable): Response {
        val collections = collectionRepository.findAllByOwner(user.id)

        val cipherCollections = collections.map {
            CipherCollection(
                id = it.id,
                owner = it.owner,
                name = it.name
            )
        }

        return ResponseHandler.generateResponse(
            data = cipherCollections,
            status = HttpStatus.OK
        )
    }

    @GetMapping("/{id}")
    fun getCollection(
        @AuthorizedUser user: UserTable,
        @PathVariable id: UUID
    ): Response {
        val collection = collectionRepository.findByIdAndOwner(id, user.id)
            ?: return ResponseError.NOT_FOUND.toResponse()

        val cipherCollection = CipherCollection(
            id = collection.id,
            owner = collection.owner,
            name = collection.name
        )

        return ResponseHandler.generateResponse(cipherCollection, HttpStatus.OK)
    }

    @PatchMapping("/{id}")
    fun updateCollection(
        @AuthorizedUser user: UserTable,
        @PathVariable id: UUID,
        @RequestBody collection: CreateCollectionRequest
    ): Response {
        collectionRepository.save(
            CollectionTable(
                id = id,
                name = collection.name,
                owner = user.id
            )
        )

        // prepare response
        val response = InsertResponse(collection.id)

        return ResponseHandler.generateResponse(response, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteCollection(
        @AuthorizedUser user: UserTable,
        @PathVariable id: UUID
    ): Response {
        val collection = collectionRepository.findByIdAndOwner(id, user.id)
            ?: return ResponseError.NOT_FOUND.toResponse()

        collectionRepository.delete(collection)

        // prepare response
        val response = InsertResponse(collection.id)

        return ResponseHandler.generateResponse(response, HttpStatus.OK)
    }
}
