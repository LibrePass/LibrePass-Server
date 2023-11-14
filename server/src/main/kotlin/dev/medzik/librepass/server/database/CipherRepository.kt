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
     * Get a list of all ciphers owned by the user.
     *
     * @param owner The user identifier.
     * @return A list of all ciphers owned by the user.
     */
    @Query("SELECT p FROM #{#entityName} p WHERE p.owner = :owner ORDER BY p.lastModified DESC")
    fun getAll(
        @Param("owner") owner: UUID
    ): List<CipherTable>

    /**
     * Check if a cipher exists and is owned by the user.
     * @param id The cipher identifier.
     * @param owner THe user identifier.
     * @return True if the cipher exists and is owned by the user, false otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM #{#entityName} p WHERE p.id = :id AND p.owner = :owner)")
    fun checkIfCipherExistsAndOwnedBy(
        @Param("id") id: UUID,
        @Param("owner") owner: UUID
    ): Boolean

    /**
     * Get all user cipher ids.
     *
     * @param owner The user identifier.
     * @return A list of all user cipher IDs.
     */
    @Query("SELECT p.id FROM #{#entityName} p WHERE p.owner = :owner")
    fun getAllIds(
        @Param("owner") owner: UUID
    ): List<UUID>

    /**
     * Update cipher data.
     *
     * @param id The cipher identifier.
     * @param data The new cipher data.
     */
    @Transactional
    @Modifying
    @Query("UPDATE #{#entityName} p SET p.data = :data WHERE p.id = :id")
    fun updateData(
        @Param("id") id: UUID,
        @Param("data") data: String
    )

    /** Remove all tokens owned by the user */
    @Transactional
    @Modifying
    fun deleteAllByOwner(owner: UUID)
}
