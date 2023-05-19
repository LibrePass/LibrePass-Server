package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.librepass.server.components.AuthorizedUser
import dev.medzik.librepass.server.database.CollectionTable
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.server.services.CollectionService
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseError
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.types.api.cipher.InsertResponse
import dev.medzik.librepass.types.api.collection.CreateCollectionRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/collection")
class CollectionController {
    @Autowired
    private lateinit var collectionService: CollectionService

    @PutMapping
    fun insertCollection(
        @AuthorizedUser user: UserTable?,
        @RequestBody collection: CreateCollectionRequest
    ): Response {
        if (user == null) return ResponseError.Unauthorized

        collectionService.insertCollection(
            CollectionTable(
                id = collection.id,
                name = collection.name,
                owner = user.id
            )
        )

        return ResponseHandler.generateResponse(InsertResponse(collection.id), HttpStatus.CREATED)
    }

    @GetMapping
    fun getAllCollections(@AuthorizedUser user: UserTable?): Response {
        if (user == null) return ResponseError.Unauthorized
        val collections = collectionService.getAllCollections(user.id)
        return ResponseHandler.generateResponse(collections, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getCollection(
        @AuthorizedUser user: UserTable?,
        @PathVariable id: UUID
    ): Response {
        if (user == null) return ResponseError.Unauthorized
        val collection = collectionService.getCollection(id, user.id) ?: return ResponseError.NotFound
        return ResponseHandler.generateResponse(collection, HttpStatus.OK)
    }

    @PatchMapping("/{id}")
    fun updateCollection(
        @AuthorizedUser user: UserTable?,
        @PathVariable id: UUID,
        @RequestBody collection: CreateCollectionRequest
    ): Response {
        if (user == null) return ResponseError.Unauthorized

        collectionService.updateCollection(
            CollectionTable(
                id = id,
                name = collection.name,
                owner = user.id
            )
        )

        return ResponseHandler.generateResponse(InsertResponse(collection.id), HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteCollection(
        @AuthorizedUser user: UserTable?,
        @PathVariable id: UUID
    ): Response {
        if (user == null) return ResponseError.Unauthorized
        val collection = collectionService.getCollection(id, user.id) ?: return ResponseError.NotFound
        collectionService.deleteCollection(collection)
        return ResponseHandler.generateResponse(InsertResponse(collection.id), HttpStatus.OK)
    }
}
