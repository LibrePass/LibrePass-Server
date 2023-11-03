package dev.medzik.librepass.server.database

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
}
