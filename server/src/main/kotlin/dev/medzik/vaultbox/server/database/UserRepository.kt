package dev.medzik.vaultbox.server.database

import java.util.*

interface UserRepository : org.springframework.data.repository.CrudRepository<UserTable, UUID> {
    fun findByEmail(email: String): UserTable?
}
