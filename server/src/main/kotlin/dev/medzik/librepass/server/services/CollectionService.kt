package dev.medzik.librepass.server.services

import dev.medzik.librepass.server.database.CollectionRepository
import dev.medzik.librepass.server.database.CollectionTable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class CollectionService {
    @Autowired
    private lateinit var collectionRepository: CollectionRepository

    fun insertCollection(collection: CollectionTable) {
        collectionRepository.save(collection)
    }

    fun getAllCollections(owner: UUID): List<CollectionTable> =
        collectionRepository.findAllByOwner(owner)

    fun getCollection(id: UUID, owner: UUID): CollectionTable? =
        collectionRepository.findByIdAndOwner(id, owner)

    fun updateCollection(collection: CollectionTable): CollectionTable =
        collectionRepository.save(collection)

    fun deleteCollection(collection: CollectionTable) =
        collectionRepository.delete(collection)
}
