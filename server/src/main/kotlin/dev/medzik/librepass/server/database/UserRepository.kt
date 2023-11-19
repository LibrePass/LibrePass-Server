package dev.medzik.librepass.server.database

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

/** Repository for the [UserTable]. */
interface UserRepository : CrudRepository<UserTable, UUID> {
    /**
     * Find user by email.
     *
     * @param email The email address of the user.
     */
    fun findByEmail(email: String): UserTable?

    /**
     * Delete accounts with unverified email addresses that were used before the specified date.
     *
     * @param createdBefore The accounts created before the specified date will be deleted.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} u WHERE u.emailVerified = false AND u.created < :createdBefore")
    fun deleteUnverifiedUsers(createdBefore: Date)
}
