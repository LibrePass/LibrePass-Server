package dev.medzik.librepass.types.api.user

import dev.medzik.libcrypto.AesCbc
import dev.medzik.libcrypto.Argon2Hash
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
    val newProtectedEncryptionKey: String,
    // new argon2 parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int
)

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
