package dev.medzik.librepass.server.database

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
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
    // Argon2id parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    // X25519 public key
    @Column(columnDefinition = "TEXT")
    val passwordHint: String? = null,
    val publicKey: String,
    // 2FA
    val twoFactorEnabled: Boolean = false,
    val twoFactorSecret: String? = null,
    val twoFactorRecoveryCode: String? = null,
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @Temporal(TemporalType.TIMESTAMP)
    val lastPasswordChange: Date = Date(),
)
