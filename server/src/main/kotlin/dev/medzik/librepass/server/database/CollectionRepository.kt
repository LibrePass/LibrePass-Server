package dev.medzik.librepass.server.database

import org.springframework.data.repository.CrudRepository
import java.util.*

/**
 * Repository for collections. It is used to interact with the database.
 * @see CollectionTable
 */
interface CollectionRepository : CrudRepository<CollectionTable, UUID> {
    /**
     * Find a collection by its ID and owner.
     * @param id The ID of the collection.
     * @param owner The owner of the collection.
     * @return The collection with the given ID and owner, or null if it doesn't exist.
     */
    fun findByIdAndOwner(id: UUID, owner: UUID): CollectionTable?

    /**
     * Find all collections owned by the given user.
     * @param owner The owner of the collections.
     * @return A list of all collections owned by the given user.
     */
    fun findAllByOwner(owner: UUID): List<CollectionTable>

    fun deleteByIdAndOwner(id: UUID, owner: UUID)
}
