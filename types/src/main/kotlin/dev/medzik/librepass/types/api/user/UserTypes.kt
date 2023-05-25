package dev.medzik.librepass.types.api.user

import dev.medzik.libcrypto.AesCbc
import dev.medzik.libcrypto.Argon2Hash
import kotlinx.serialization.Serializable

/**
 * Request for changing user password.
 * @property oldPassword Old user password. (hashed)
 * @property newPassword New user password. (hashed)
 * @property newEncryptionKey New user encryption key. (encrypted using new password)
 * @property parallelism Argon2id parallelism.
 * @property memory Argon2id memory.
 * @property iterations Argon2id iterations.
 * @property version Argon2id version.
 */
@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
    val newEncryptionKey: String,
    // new argon2 parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int
)

/**
 * Response of user secrets.
 * @property encryptionKey User encryption key. (encrypted using user password)
 * @property publicKey User public key.
 * @property privateKey User private key. (encrypted using encryption key)
 */
@Serializable
data class UserSecretsResponse(
    val encryptionKey: String,
    val publicKey: String,
    val privateKey: String
) {
    /**
     * Decrypt user secrets. (private key and encryption key)
     */
    fun decrypt(password: Argon2Hash): UserSecretsResponse {
        val decryptedEncryptionKey = AesCbc.decrypt(encryptionKey, password.toHexHash())

        return UserSecretsResponse(
            encryptionKey = decryptedEncryptionKey,
            privateKey = AesCbc.decrypt(privateKey, decryptedEncryptionKey),
            publicKey = publicKey,
        )
    }

    /**
     * Encrypt user secrets before sending to server. (private key and encryption key)
     */
    fun encrypt(password: Argon2Hash): UserSecretsResponse {
        val secretKey = password.toHexHash()

        return UserSecretsResponse(
            encryptionKey = AesCbc.encrypt(encryptionKey, secretKey),
            privateKey = AesCbc.encrypt(privateKey, secretKey),
            publicKey = publicKey,
        )
    }
}
