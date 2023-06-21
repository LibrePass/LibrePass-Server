package dev.medzik.librepass.types.api.cipher

import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import dev.medzik.librepass.types.cipher.EncryptedCipher
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class InsertResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)

@Serializable
data class SyncResponse(
    val ids: List<
        @Serializable(with = UUIDSerializer::class)
        UUID
        >,
    val ciphers: List<EncryptedCipher>
)
