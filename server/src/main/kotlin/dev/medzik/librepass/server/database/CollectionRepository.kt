package dev.medzik.librepass.server.database

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.CrudRepository
import java.util.*

/** Repository for [CollectionTable]. */
interface CollectionRepository : CrudRepository<CollectionTable, UUID> {
    /**
     * Finds a collection by its [id] and [owner].
     */
    fun findByIdAndOwner(
        id: UUID,
        owner: UUID
    ): CollectionTable?

    /**
     * Finds all collections owned by the given [user].
     */
    fun findAllByOwner(user: UUID): List<CollectionTable>

    /**
     * Deletes all collections owned by the [user].
     */
    @Transactional
    @Modifying
    fun deleteAllByOwner(user: UUID)
}
