package dev.medzik.librepass.server.database

import dev.medzik.libcrypto.Salt
import jakarta.persistence.*
import org.apache.commons.codec.binary.Hex
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "tokenTable")
data class TokenTable(
    @Id
    val token: String = generateToken(),
    val owner: UUID,

    val lastIp: String,

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @Temporal(TemporalType.TIMESTAMP)
    val lastUsed: Date = Date()
) {
    companion object {
        fun generateToken() = "lp_" + Hex.encodeHexString(Salt.generate(32))
    }
}
