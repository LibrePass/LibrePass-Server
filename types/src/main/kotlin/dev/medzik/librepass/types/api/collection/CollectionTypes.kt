package dev.medzik.librepass.types.api.collection

import java.util.*

data class CreateCollectionRequest(
    val id: UUID = UUID.randomUUID(),
    val name: String,
)

data class CipherCollection(
    val id: UUID,
    val name: String,
    val owner: UUID
)
