package dev.medzik.librepass.types.api.collection

import com.google.gson.annotations.JsonAdapter
import dev.medzik.librepass.types.adapters.DateAdapter
import java.util.*

data class CreateCollectionRequest(
    val id: UUID = UUID.randomUUID(),
    val name: String,
)

data class CipherCollection(
    val id: UUID,
    val name: String,
    val owner: UUID,
    @JsonAdapter(DateAdapter::class)
    val created: Date,
    @JsonAdapter(DateAdapter::class)
    val lastModified: Date
)
