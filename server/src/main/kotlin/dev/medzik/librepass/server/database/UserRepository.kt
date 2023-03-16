package dev.medzik.librepass.server.database

import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<UserTable, UUID> {
    /**
     * Finds user by email.
     * @param email The email of the user.
     */
    fun findByEmail(email: String): UserTable?
}
