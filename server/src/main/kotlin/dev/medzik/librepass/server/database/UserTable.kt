package dev.medzik.librepass.server.database

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table
data class UserTable(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, columnDefinition = "TEXT")
    val email: String,
    val emailVerified: Boolean = false,
    val emailVerificationCode: String? = null,
    val emailVerificationCodeExpiresAt: Date? = null,

    @Column(columnDefinition = "TEXT")
    val passwordHint: String? = null,
    @Temporal(TemporalType.TIMESTAMP)
    val lastPasswordChange: Date = Date(),

    // Argon2id parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,

    // Curve25519 public key
    val publicKey: String,

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    val lastModified: Date = Date(),
)
