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

    /** Remove unverified users who have not verified their account, which was [createdBefore]. */
    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} u WHERE u.emailVerified = false AND u.created < :createdBefore")
    fun deleteUnverifiedUsers(createdBefore: Date)
}
