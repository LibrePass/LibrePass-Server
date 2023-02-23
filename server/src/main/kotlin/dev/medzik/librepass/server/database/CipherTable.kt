package dev.medzik.librepass.server.database

import dev.medzik.librepass.types.api.EncryptedCipher
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "ciphers")
class CipherTable {
    @Id
    var id: UUID = UUID.randomUUID()

    lateinit var owner: UUID

    lateinit var type: Number
    @Column(columnDefinition = "TEXT")
    lateinit var data: String

    var favorite: Boolean = false
    var directory: UUID? = null
    var rePrompt: Boolean = false

    // TODO: fix created and lastModified fields
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
//    @Column(nullable = false)
    var created: Date? = null

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
//    @Column(nullable = false)
    var lastModified: Date? = null

    fun from(cipher: EncryptedCipher) {
        this.id = cipher.id

        this.owner = cipher.owner

        this.type = cipher.type
        this.data = cipher.data

        this.favorite = cipher.favorite
        this.directory = cipher.directory
        this.rePrompt = cipher.rePrompt
    }
}
