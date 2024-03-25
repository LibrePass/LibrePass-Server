package dev.medzik.librepass.server.database

import dev.medzik.librepass.types.cipher.Cipher.Companion.currentFixedDate
import dev.medzik.librepass.types.cipher.EncryptedCipher
import jakarta.persistence.*
import jakarta.validation.constraints.Max
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

@Entity
@Table
data class CipherTable(
    @Id
    val id: UUID = UUID.randomUUID(),
    val owner: UUID,
    val type: Int,
    @Column(columnDefinition = "TEXT")
    @Max(5000)
    val data: String,
    val favorite: Boolean = false,
    val collection: UUID? = null,
    val rePrompt: Boolean = false,
    val version: Int = 1,

    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = currentFixedDate(),
    @Temporal(TemporalType.TIMESTAMP)
    val lastModified: Date = currentFixedDate(),
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val lastServerSync: Date = currentFixedDate()
) {
    constructor(cipher: EncryptedCipher) : this(
        id = cipher.id,
        owner = cipher.owner,
        type = cipher.type,
        data = cipher.protectedData,
        favorite = cipher.favorite,
        collection = cipher.collection,
        rePrompt = cipher.rePrompt,
        created = cipher.created ?: currentFixedDate(),
        lastModified = cipher.lastModified ?: currentFixedDate()
    )

    /** Convert to [EncryptedCipher] object. This is used to send data to the client. */
    fun toEncryptedCipher() =
        EncryptedCipher(
            id = id,
            owner = owner,
            type = type,
            protectedData = data,
            favorite = favorite,
            collection = collection,
            rePrompt = rePrompt,
            created = created,
            lastModified = lastModified
        )
}
