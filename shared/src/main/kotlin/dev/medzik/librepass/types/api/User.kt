package dev.medzik.librepass.types.api

import java.util.*

/**
 * Represents Cipher data that has been re-encrypted.
 *
 * @property id The identifier of the cipher.
 * @property data The encrypted cipher data.
 */
data class ChangePasswordCipherData(
    val id: UUID,
    val data: String
)

/**
 * Request for change password endpoint.
 *
 * @property oldSharedKey The shared key with server, computed with the old public key,
 * @property newPasswordHint The new password hint (optional but recommended).
 * @property newPublicKey The new X25519 public key.
 * @property newSharedKey The shared key with server, computed with the new public key.
 * @property parallelism The number of threads to use for calculating argon2 hash.
 * @property memory The memory to use for calculating argon2 hash.
 * @property iterations The number of iterations for calculating argon2 hash.
 * @property ciphers The re-encrypted all ciphers because the key to encrypt it changed.
 */
data class ChangePasswordRequest(
    val oldSharedKey: String,
    val newPasswordHint: String?,
    val newPublicKey: String,
    val newSharedKey: String,
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val ciphers: List<ChangePasswordCipherData>
)

/**
 * Request for endpoint that setups two-factor authentication.
 *
 * @property sharedKey The shared key with server, to verify authentication.
 * @property secret The OTP secret.
 * @property code The OTP code generated using the [secret].
 */
data class SetupTwoFactorRequest(
    val sharedKey: String,
    val secret: String,
    val code: String
)

/**
 * Request for endpoint that setups two-factor authentication.
 *
 * @property sharedKey The shared key with server, to verify authentication.
 * @property code The OTP code (if 2fa is set)
 */
data class DeleteAccountRequest(
    val sharedKey: String,
    val code: String?
)

/**
 * Response from endpoint that setups two-factor authentication.
 *
 * @property recoveryCode The generated recovery code for emergency access to your account.
 */
data class SetupTwoFactorResponse(
    val recoveryCode: String
)
