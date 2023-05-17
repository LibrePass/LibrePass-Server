package dev.medzik.librepass.types.api.user

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
    val newEncryptionKey: String,
    val newPrivateKey: String,
    // new argon2 parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int
)
