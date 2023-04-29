package dev.medzik.librepass.server.database

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface CipherRepository : CrudRepository<CipherTable, UUID> {
    /**
     * Get a list of all ciphers owned by the user.
     * @param owner The owner of the ciphers.
     * @return A list of all ciphers owned by the user.
     */
    @Query("SELECT p FROM #{#entityName} p WHERE p.owner = :owner ORDER BY p.lastModified DESC")
    fun getAll(@Param("owner") owner: UUID): List<CipherTable>

    /**
     * Get the owner of a cipher.
     * @param id The id of the cipher.
     * @return The owner of the cipher.
     */
    @Query("SELECT p.owner FROM #{#entityName} p WHERE p.id = :id")
    fun getOwnerOfCipher(@Param("id") id: UUID): UUID?

    /**
     * Check if a cipher exists and is owned by the user.
     * @param id The id of the cipher.
     * @param owner The owner of the cipher.
     * @return True if the cipher exists and is owned by the user, false otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM #{#entityName} p WHERE p.id = :id AND p.owner = :owner)")
    fun checkIfCipherExistsAndOwnedBy(@Param("id") id: UUID, @Param("owner") owner: UUID): Boolean

    /**
     * Get a list of all cipher ids that are in a collection owned by the user.
     * @param collection The collection to get ciphers from.
     * @param owner The owner of the collection.
     * @return A list of all ciphers that are in a collection owned by the user.
     */
    @Query("SELECT p.id FROM #{#entityName} p WHERE p.collection = :collection AND p.owner = :owner")
    fun getAllIDs(@Param("collection") collection: UUID, @Param("owner") owner: UUID): List<UUID>
}
