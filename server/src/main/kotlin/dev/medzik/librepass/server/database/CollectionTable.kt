package dev.medzik.librepass.server.database

import jakarta.persistence.*
import jakarta.validation.constraints.Max
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

@Entity
@Table
data class CollectionTable(
    @Id
    val id: UUID = UUID.randomUUID(),
    val owner: UUID,
    @Max(32)
    val name: String,
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val created: Date = Date(),
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val lastModified: Date = Date()
)
