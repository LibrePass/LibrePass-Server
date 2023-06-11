package dev.medzik.librepass.types.api.user

import dev.medzik.librepass.types.api.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ChangePasswordRequest(
    val newPasswordHint: String?,
    val newPublicKey: String,
    val sharedKey: String,
    // New Argon2 parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int,
    // Update ciphers data due to password change
    val ciphers: List<ChangePasswordCipherData>
)

@Serializable
data class ChangePasswordCipherData(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val data: String
)
