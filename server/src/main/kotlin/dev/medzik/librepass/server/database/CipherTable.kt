package dev.medzik.librepass.server.database

import dev.medzik.librepass.types.cipher.EncryptedCipher
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "ciphers")
data class CipherTable(
    @Id
    val id: UUID = UUID.randomUUID(),

    val owner: UUID,

    val type: Int,
    @Column(columnDefinition = "TEXT")
    val data: String,

    val favorite: Boolean = false,
    val collection: UUID? = null,
    val rePrompt: Boolean = false,

    val version: Int = 1,

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    val lastModified: Date = Date()
) {
    constructor(cipher: EncryptedCipher) : this(
        id = cipher.id,
        owner = cipher.owner,
        type = cipher.type,
        data = cipher.protectedData,
        favorite = cipher.favorite,
        collection = cipher.collection,
        rePrompt = cipher.rePrompt,
        version = cipher.version,
        created = cipher.created ?: Date(),
        lastModified = cipher.lastModified ?: Date()
    )

    /**
     * Convert to [EncryptedCipher] object. This is used to send data to the client.
     */
    fun toEncryptedCipher() = EncryptedCipher(
        id = id,
        owner = owner,
        type = type,
        protectedData = data,
        favorite = favorite,
        collection = collection,
        rePrompt = rePrompt,
        version = version,
        created = created,
        lastModified = lastModified
    )
}
