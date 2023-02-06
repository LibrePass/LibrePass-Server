package dev.medzik.vaultbox.server.database

import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<UserTable, UUID> {
    fun findByEmail(email: String): UserTable?
}
