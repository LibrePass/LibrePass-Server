package dev.medzik.librepass.server.database

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.util.*

@Entity
@Table(name = "users")
class UserTable {
    @Id
    var id: UUID = UUID.randomUUID()

    @Column(unique = true, columnDefinition = "TEXT")
    lateinit var email: String
    var emailVerified: Boolean = false

    @Column(columnDefinition = "TEXT")
    lateinit var password: String
    lateinit var passwordSalt: ByteArray
    var passwordHint: String? = null

    @Column(columnDefinition = "TEXT")
    lateinit var encryptionKey: String

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false, updatable = false)
    var created: Date? = null
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false, updatable = false)
    var lastModified: Date? = null
}
