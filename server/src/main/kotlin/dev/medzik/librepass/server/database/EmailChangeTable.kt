package dev.medzik.librepass.server.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table
data class EmailChangeTable(
    @Id
    val owner: UUID,
    @Column(unique = true, columnDefinition = "TEXT")
    val newEmail: String,
    val code: String,
    val codeExpiresAt: Date,
    @Column(columnDefinition = "TEXT")
    val newCiphers: String
)
