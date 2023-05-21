package dev.medzik.librepass.server.database

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * Repository for the ciphers. It is used to interact with the database.
 * @see CipherTable
 */
interface CipherRepository : CrudRepository<CipherTable, UUID> {
    /**
     * Get a list of all ciphers owned by the user.
     * @param owner The owner of the ciphers.
     * @return A list of all ciphers owned by the user.
     */
    @Query("SELECT p FROM #{#entityName} p WHERE p.owner = :owner ORDER BY p.lastModified DESC")
    fun getAll(@Param("owner") owner: UUID): List<CipherTable>

    /**
     * Check if a cipher exists and is owned by the user.
     * @param id The id of the cipher.
     * @param owner The owner of the cipher.
     * @return True if the cipher exists and is owned by the user, false otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM #{#entityName} p WHERE p.id = :id AND p.owner = :owner)")
    fun checkIfCipherExistsAndOwnedBy(@Param("id") id: UUID, @Param("owner") owner: UUID): Boolean
}
