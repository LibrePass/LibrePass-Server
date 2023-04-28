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
    var collection: UUID? = null
    var rePrompt: Boolean = false

    @CreatedDate
    lateinit var created: Date

    @LastModifiedDate
    lateinit var lastModified: Date

    fun toEncryptedCipher(): EncryptedCipher = EncryptedCipher(
        id = this.id,
        owner = this.owner,
        type = this.type.toInt(),
        data = this.data,
        favorite = this.favorite,
        collection = this.collection,
        rePrompt = this.rePrompt,
        created = this.created,
        lastModified = this.lastModified
    )

    fun toJson() = toEncryptedCipher().toJson()

    fun from(cipher: EncryptedCipher) {
        this.id = cipher.id

        this.owner = cipher.owner

        this.type = cipher.type
        this.data = cipher.data

        this.favorite = cipher.favorite
        this.collection = cipher.collection
        this.rePrompt = cipher.rePrompt

        this.created = cipher.created ?: Date()
        this.lastModified = cipher.lastModified ?: Date()
    }
}
