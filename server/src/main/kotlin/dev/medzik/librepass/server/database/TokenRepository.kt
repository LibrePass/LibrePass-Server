package dev.medzik.librepass.server.database

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TokenRepository : CrudRepository<TokenTable, String> {
    @Query("SELECT t FROM #{#entityName} t WHERE t.lastUsed < :lastUsedBefore")
    fun findAllByLastUsedBefore(lastUsedBefore: Date): List<TokenTable>
}
