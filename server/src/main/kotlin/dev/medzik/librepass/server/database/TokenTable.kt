package dev.medzik.librepass.server.database

import dev.medzik.libcrypto.Salt
import jakarta.persistence.*
import org.apache.commons.codec.binary.Hex
import java.util.*

@Entity
@Table(name = "tokenTable")
data class TokenTable(
    @Id
    val token: String = generateToken(),
    val owner: UUID,

    val lastIp: String,

    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @Temporal(TemporalType.TIMESTAMP)
    val lastUsed: Date = Date()
) {
    companion object {
        fun generateToken(): String {
            return "lp_" + Hex.encodeHexString(Salt.generate(32))
        }
    }
}
