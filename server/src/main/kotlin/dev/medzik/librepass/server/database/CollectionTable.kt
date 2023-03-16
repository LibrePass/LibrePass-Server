package dev.medzik.librepass.server.database

import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "collections")
class CollectionTable {
    @Id
    var id: UUID = UUID.randomUUID()

    lateinit var owner: UUID
    lateinit var name: String
}
