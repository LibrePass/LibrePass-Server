package dev.medzik.librepass.server.database

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.util.*

@Entity
@Table(name = "users")
data class UserTable (
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, columnDefinition = "TEXT")
    val email: String,
    val emailVerified: Boolean = false,

    // argon2id parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,

    @Column(columnDefinition = "TEXT")
    val password: String,
    val passwordHint: String? = null,

    @Column(columnDefinition = "TEXT")
    val encryptionKey: String,

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false, updatable = false)
    val created: Date? = null,
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false, updatable = false)
    val lastModified: Date? = null,
)
