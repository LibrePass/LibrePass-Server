package dev.medzik.librepass.server.database

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.validator.constraints.Range
import java.util.*

@Entity
@Table
data class UserTable(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(unique = true, columnDefinition = "TEXT")
    @Email
    val email: String,
    val emailVerified: Boolean = false,
    val emailVerificationCode: String? = null,
    val emailVerificationCodeExpiresAt: Date? = null,
    // Argon2id parameters
    @Range(min = 1, max = 10)
    val parallelism: Int,
    @Range(min = 20 * 1024, max = 150 * 1024)
    val memory: Int,
    @Range(min = 1, max = 10)
    val iterations: Int,
    // X25519 public key
    val publicKey: String,
    @Column(columnDefinition = "TEXT")
    @Max(100)
    val passwordHint: String? = null,
    // 2FA
    val twoFactorEnabled: Boolean = false,
    @Max(32)
    val twoFactorSecret: String? = null,
    val twoFactorRecoveryCode: String? = null,
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @Temporal(TemporalType.TIMESTAMP)
    val lastPasswordChange: Date = Date(),
)
