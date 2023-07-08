package dev.medzik.librepass.server.database

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

@Entity
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

    // Curve25519 public key
    val publicKey: String,

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val lastModified: Date = Date()
)
