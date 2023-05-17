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
    val emailVerificationCode: String? = null,
    val emailVerificationCodeExpiresAt: Date? = null,

    // argon2id parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,

    @Column(columnDefinition = "TEXT")
    val password: String,
    val passwordHint: String? = null,
    @Temporal(TemporalType.TIMESTAMP)
    val lastPasswordChange: Date? = null,

    @Column(columnDefinition = "TEXT")
    val encryptionKey: String,

    // RSA key pair
    @Column(columnDefinition = "TEXT")
    val publicKey: String,
    @Column(columnDefinition = "TEXT")
    val privateKey: String,

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false, updatable = false)
    val created: Date? = null,
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false, updatable = false)
    val lastModified: Date? = null,
)
