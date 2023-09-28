package dev.medzik.librepass.server.database

import dev.medzik.libcrypto.Random
import dev.medzik.librepass.utils.toHexString
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.util.*

@Entity
@Table
data class TokenTable(
    @Id
    val token: String = generateToken(),
    val owner: UUID,
    val confirmed: Boolean,

    val lastIp: String,

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @Temporal(TemporalType.TIMESTAMP)
    val lastUsed: Date = Date()
) {
    companion object {
        fun generateToken() = "lp_" + Random.randBytes(32).toHexString()
    }
}
