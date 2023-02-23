package dev.medzik.librepass.server.database

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface CipherRepository : CrudRepository<CipherTable, UUID> {
    fun findAllByOwner(owner: UUID): List<CipherTable>
    @Query("SELECT p.id FROM #{#entityName} p WHERE p.owner = :owner")
    fun getAllIds(@Param("owner") owner: UUID): List<UUID>

    @Query("SELECT p.owner FROM #{#entityName} p WHERE p.id = :id")
    fun getOwnerOfCipher(@Param("id") id: UUID): UUID

    @Query("SELECT EXISTS(SELECT 1 FROM #{#entityName} p WHERE p.id = :id AND p.owner = :owner)")
    fun checkIfCipherExistsAndOwnedBy(@Param("id") id: UUID, @Param("owner") owner: UUID): Boolean
}
