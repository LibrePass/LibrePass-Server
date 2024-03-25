package dev.medzik.librepass.server.controllers.api

import dev.medzik.librepass.server.components.annotations.AuthorizedUser
import dev.medzik.librepass.server.controllers.advice.ServerException
import dev.medzik.librepass.server.database.CollectionRepository
import dev.medzik.librepass.server.database.CollectionTable
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.types.api.CipherCollection
import dev.medzik.librepass.types.api.CollectionIdResponse
import dev.medzik.librepass.types.api.CreateCollectionRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/collection")
class CollectionController @Autowired constructor(
    private val collectionRepository: CollectionRepository
) {
    @PutMapping
    fun saveCollection(
        @AuthorizedUser user: UserTable,
        @Valid @RequestBody collection: CreateCollectionRequest
    ): Response {
        if (collection.name.length > 32)
            throw ServerException.InvalidCollection("name is too long")

        collectionRepository.save(
            CollectionTable(
                id = collection.id,
                name = collection.name,
                owner = user.id
            )
        )

        return ResponseHandler.generateResponse(CollectionIdResponse(collection.id), HttpStatus.CREATED)
    }

    @GetMapping
    fun getAllCollections(
        @AuthorizedUser user: UserTable
    ): Response {
        val collections = collectionRepository.findAllByOwner(user.id)

        val cipherCollections = collections.map {
            CipherCollection(
                id = it.id,
                owner = it.owner,
                name = it.name,
                created = it.created,
                lastModified = it.lastModified
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
        val collection =
            collectionRepository.findByIdAndOwner(id, user.id)
                ?: throw ServerException.CollectionNotFound()

        val cipherCollection = CipherCollection(
            id = collection.id,
            owner = collection.owner,
            name = collection.name,
            created = collection.created,
            lastModified = collection.lastModified
        )

        return ResponseHandler.generateResponse(cipherCollection, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteCollection(
        @AuthorizedUser user: UserTable,
        @PathVariable id: UUID
    ): Response {
        val collection = collectionRepository.findByIdAndOwner(id, user.id) ?: throw ServerException.CollectionNotFound()

        collectionRepository.delete(collection)

        return ResponseHandler.generateResponse(CollectionIdResponse(collection.id), HttpStatus.OK)
    }
}
