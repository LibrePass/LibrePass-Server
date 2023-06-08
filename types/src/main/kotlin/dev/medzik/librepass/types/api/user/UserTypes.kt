package dev.medzik.librepass.types.api.user

import dev.medzik.libcrypto.AES
import dev.medzik.libcrypto.Argon2Hash
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
    val newProtectedPrivateKey: String,
    // New Argon2 parameters
    val parallelism: Int,
    val memory: Int,
    val iterations: Int,
    val version: Int
)

@Serializable
data class UserSecretsResponse(
    val publicKey: String,
    val protectedPrivateKey: String
) {
    /**
     * Decrypts user secrets.
     */
    fun decrypt(password: Argon2Hash): UserSecrets {
        val secretKey = password.toHexHash()

        return UserSecrets(
            privateKey = AES.decrypt(AES.GCM, secretKey, protectedPrivateKey),
            publicKey = publicKey,
        )
    }
}

data class UserSecrets(
    val publicKey: String,
    val privateKey: String
) {
    /**
     * Encrypts user secrets.
     */
    fun encrypt(password: Argon2Hash): UserSecretsResponse {
        val secretKey = password.toHexHash()

        return UserSecretsResponse(
            publicKey = publicKey,
            protectedPrivateKey = AES.encrypt(AES.GCM, secretKey, privateKey)
        )
    }
}
