package dev.medzik.librepass.server.database

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface CollectionRepository : CrudRepository<CollectionTable, UUID> {
    /**
     * Find all collections owned by the given user.
     * @param owner The owner of the collections.
     * @return A list of collections owned by the given user.
     */
    fun findAllByOwner(owner: UUID): List<CollectionTable>

    /**
     * Find a collection by its ID and owner.
     * @param id The ID of the collection.
     * @param owner The owner of the collection.
     * @return The collection with the given ID and owner, or null if it doesn't exist.
     */
    fun findByIdAndOwner(id: UUID, owner: UUID): CollectionTable?

    /**
     * Find all IDs of collections owned by the given user.
     * @param owner The owner of the collections.
     * @return A list of IDs of collections owned by the given user.
     */
    @Query("SELECT p.id FROM #{#entityName} p WHERE p.owner = :owner")
    fun findAllIdsByOwner(@Param("owner") owner: UUID): List<UUID>
}
