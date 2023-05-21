package dev.medzik.librepass.server.database

import dev.medzik.librepass.types.EncryptedCipher
import dev.medzik.librepass.types.api.serializers.DateSerializer
import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import jakarta.persistence.*
import kotlinx.serialization.Serializable
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "ciphers")
@Serializable
data class CipherTable(
    @Id
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),

    @Serializable(with = UUIDSerializer::class)
    val owner: UUID,

    val type: Int,
    @Column(columnDefinition = "TEXT")
    val data: String,

    val favorite: Boolean = false,
    @Serializable(with = UUIDSerializer::class)
    val collection: UUID? = null,
    val rePrompt: Boolean = false,

    @CreatedDate
    @Serializable(with = DateSerializer::class)
    val created: Date = Date(),
    @LastModifiedDate
    @Serializable(with = DateSerializer::class)
    val lastModified: Date = Date()
) {
    constructor(cipher: EncryptedCipher) : this(
        id = cipher.id,
        owner = cipher.owner,
        type = cipher.type,
        data = cipher.data,
        favorite = cipher.favorite,
        collection = cipher.collection,
        rePrompt = cipher.rePrompt,
        created = cipher.created ?: Date(),
        lastModified = cipher.lastModified ?: Date()
    )

    fun toEncryptedCipher() = EncryptedCipher(
        id = id,
        owner = owner,
        type = type,
        data = data,
        favorite = favorite,
        collection = collection,
        rePrompt = rePrompt,
        created = created,
        lastModified = lastModified
    )
}
