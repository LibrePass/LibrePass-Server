package dev.medzik.librepass.types.api.cipher

import dev.medzik.librepass.types.cipher.EncryptedCipher
import java.util.*

data class InsertResponse(
    val id: UUID
)

data class SyncResponse(
    val ids: List<UUID>,
    val ciphers: List<EncryptedCipher>
)
