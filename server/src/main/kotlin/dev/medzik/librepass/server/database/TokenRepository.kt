package dev.medzik.librepass.server.database

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

/** Repository for the [TokenTable]. */
interface TokenRepository : CrudRepository<TokenTable, String> {
    /**
     * Deletes all unused tokens that were last used before the specified [date].
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} t WHERE t.lastUsed < :date")
    fun deleteUnused(date: Date)

    /**
     * Deletes all tokens owned by the user.
     */
    @Transactional
    @Modifying
    fun deleteAllByOwner(owner: UUID)
}
