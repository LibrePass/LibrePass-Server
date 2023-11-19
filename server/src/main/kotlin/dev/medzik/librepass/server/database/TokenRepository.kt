package dev.medzik.librepass.server.database

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

/** Repository for the [TokenTable]. */
interface TokenRepository : CrudRepository<TokenTable, String> {
    /**
     * Delete unused tokens that were used before the specified date.
     *
     * @param lastUsedBefore The tokens used before the specified date will be deleted.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} t WHERE t.lastUsed < :lastUsedBefore")
    fun deleteUnused(lastUsedBefore: Date)

    /**
     * Delete all tokens owned by the user.
     *
     * @param owner The user identifier.
     */
    @Transactional
    @Modifying
    fun deleteAllByOwner(owner: UUID)
}
