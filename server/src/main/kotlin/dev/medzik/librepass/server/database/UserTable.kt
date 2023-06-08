package dev.medzik.librepass.server.database

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "users")
data class UserTable (
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, columnDefinition = "TEXT")
    val email: String,
    val emailVerified: Boolean = false,
    val emailVerificationCode: UUID? = null,
    val emailVerificationCodeExpiresAt: Date? = null,

    // Argon2id parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,

    @Column(columnDefinition = "TEXT")
    val passwordHash: String,
    @Column(columnDefinition = "TEXT")
    val passwordHint: String? = null,
    @Temporal(TemporalType.TIMESTAMP)
    val lastPasswordChange: Date = Date(),

    // Curve25519 key pair
    @Column(columnDefinition = "TEXT")
    val publicKey: String,
    @Column(columnDefinition = "TEXT")
    val protectedPrivateKey: String,

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    val lastModified: Date = Date(),
)
