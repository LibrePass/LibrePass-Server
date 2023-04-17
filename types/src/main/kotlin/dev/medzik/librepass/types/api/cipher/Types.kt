package dev.medzik.librepass.types.api.cipher

import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class InsertResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)
