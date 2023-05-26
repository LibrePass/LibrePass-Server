package dev.medzik.librepass.server.database

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "users")
data class UserTable(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Lob
    @Column(unique = true)
    val email: String,
    val emailVerified: Boolean = false,
    val emailVerificationCode: String? = null,
    val emailVerificationCodeExpiresAt: Date? = null,

    // argon2id parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,

    @Lob
    val password: String,
    @Lob
    val passwordHint: String? = null,
    @Temporal(TemporalType.TIMESTAMP)
    val lastPasswordChange: Date = Date(),

    @Lob
    val encryptionKey: String,

    // RSA key pair
    @Lob
    val publicKey: String,
    @Lob
    val privateKey: String,

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    val lastModified: Date = Date(),
)
