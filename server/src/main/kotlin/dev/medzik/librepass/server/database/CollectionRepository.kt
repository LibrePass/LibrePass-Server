package dev.medzik.librepass.server.database

import org.springframework.data.repository.CrudRepository
import java.util.*

/** Repository for [CollectionTable]. */
interface CollectionRepository : CrudRepository<CollectionTable, UUID> {
    /**
     * Find a collection by its ID and owner.
     *
     * @param id The collection identifier.
     * @param owner The user identifier.
     * @return The collection with the given ID and owner, or null if it doesn't exist.
     */
    fun findByIdAndOwner(
        id: UUID,
        owner: UUID
    ): CollectionTable?

    /**
     * Find all collections owned by the given user.
     *
     * @param owner The user identifier.
     * @return A list of all collections owned by the given user.
     */
    fun findAllByOwner(owner: UUID): List<CollectionTable>
}
