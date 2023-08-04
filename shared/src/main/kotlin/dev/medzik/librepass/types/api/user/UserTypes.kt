package dev.medzik.librepass.types.api.user

import java.util.*

data class ChangePasswordRequest(
    val newPasswordHint: String?,
    val newPublicKey: String,
    val sharedKey: String,
    // New Argon2 parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    // Update ciphers data due to password change
    val ciphers: List<ChangePasswordCipherData>
)

data class ChangePasswordCipherData(
    val id: UUID,
    val data: String
)

data class SetupTwoFactorRequest(
    val sharedKey: String,
    val secret: String,
    val code: String
)

data class SetupTwoFactorResponse(
    val recoveryCode: String
)
