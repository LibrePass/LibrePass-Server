package dev.medzik.librepass.types.api

import java.util.*
import dev.medzik.librepass.types.cipher.EncryptedCipher

/**
 * Represents Cipher data that has been re-encrypted.
 *
 * @property id The cipher identifier.
 * @property data The encrypted cipher data. Only the "protectedData" property, not the full [EncryptedCipher].
 */
data class ChangePasswordCipherData(
    val id: UUID,
    val data: String
)

/**
 * Request for change email endpoint.
 *
 * @property newEmail The new user's email address.
 * @property oldSharedKey The shared key with server computed with the old public key.
 * @property newPublicKey The new user's X25519 public key.
 * @property newSharedKey The shared key with server computed with the new public key.
 * @property ciphers The re-encrypted all ciphers because the aes key changed.
 */
data class ChangeEmailRequest(
    val newEmail: String,
    val oldSharedKey: String,
    val newPublicKey: String,
    val newSharedKey: String,
    val ciphers: List<ChangePasswordCipherData>
)

/**
 * Request for change password endpoint.
 *
 * @property oldSharedKey The shared key with server computed with the old public key,
 * @property newPasswordHint The hint for the user's password. (optional but recommended)
 * @property newPublicKey The new user's X25519 public key.
 * @property newSharedKey The shared key with server, computed with the new public key.
 * @property parallelism The argon2id parallelism parameter.
 * @property memory The argon2id memory parameter.
 * @property iterations The argon2id iterations parameter.
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
 * @property secret The TOTP secret.
 * @property code The TOTP code generated using the [secret].
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
 * @property code The OTP code. Required only if 2-factor authentication is enabled.
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
