package dev.medzik.librepass.server.database

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

/** Repository for the [CipherTable]. */
interface CipherRepository : CrudRepository<CipherTable, UUID> {
    /**
     * Get a list of all ciphers owned by the [user].
     */
    fun getAllByOwner(user: UUID): List<CipherTable>

    /**
     * Get a list of all cipher owned by the [user] that has been updated after the given [date].
     */
    @Query("SELECT c FROM CipherTable c WHERE c.owner = :user AND c.lastServerSync >= :date")
    fun getAllByOwnerAndLastServerSync(
        user: UUID,
        date: Date
    ): List<CipherTable>

    /**
     * Get a number of ciphers owned by the [user].
     */
    fun countByOwner(user: UUID): Long

    /**
     * Check if a cipher exists and is owned by the user.
     * @param id The cipher identifier.
     * @param owner THe user identifier.
     * @return True if the cipher exists and is owned by the user, false otherwise.
     */
    fun existsByIdAndOwner(
        @Param("id") id: UUID,
        @Param("owner") owner: UUID
    ): Boolean

    /**
     * Get all cipher ids owned by the [user].
     *
     * @return A IDs list of all cipher owned by the user.
     */
    @Query("SELECT c.id FROM #{#entityName} c WHERE c.owner = :user")
    fun getAllIDs(
        @Param("user") user: UUID
    ): List<UUID>

    /**
     * Update cipher data.
     *
     * @param id The cipher identifier.
     * @param data The new cipher data.
     */
    @Transactional
    @Modifying
    @Query("UPDATE #{#entityName} c SET c.data = :data WHERE c.id = :id")
    fun updateData(
        @Param("id") id: UUID,
        @Param("data") data: String
    )

    /**
     * Delete all tokens owned by the [user].
     */
    @Transactional
    @Modifying
    fun deleteAllByOwner(user: UUID)
}
