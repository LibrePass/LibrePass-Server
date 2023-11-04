package dev.medzik.librepass.server.database

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

/** Repository for the [TokenTable]. */
interface TokenRepository : CrudRepository<TokenTable, String> {
    /** Remove unused tokens that were last used before [lastUsedBefore]. */
    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} t WHERE t.lastUsed < :lastUsedBefore")
    fun deleteUnused(lastUsedBefore: Date)
}
