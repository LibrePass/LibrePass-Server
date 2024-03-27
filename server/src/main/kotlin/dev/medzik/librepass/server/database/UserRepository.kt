package dev.medzik.librepass.server.database

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

/** Repository for the [UserTable]. */
interface UserRepository : CrudRepository<UserTable, UUID> {
    /**
     * Finds user by [email].
     */
    fun findByEmail(email: String): UserTable?

    /**
     * Deletes all accounts with unverified email addresses that were created before the specified [date].
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} u WHERE u.emailVerified = false AND u.created < :date")
    fun deleteUnverifiedUsers(date: Date)
}
