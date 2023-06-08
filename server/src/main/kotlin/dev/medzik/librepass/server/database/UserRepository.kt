package dev.medzik.librepass.server.database

import org.springframework.data.repository.CrudRepository
import java.util.*

/**
 * Repository for the users. It is used to interact with the database.
 */
interface UserRepository : CrudRepository<UserTable, UUID> {
    /**
     * Finds user by email.
     * @param email user email
     */
    fun findByEmail(email: String): UserTable?
}
