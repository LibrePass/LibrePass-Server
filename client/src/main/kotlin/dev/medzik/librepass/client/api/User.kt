package dev.medzik.librepass.client.api

import dev.medzik.libcrypto.Aes
import dev.medzik.libcrypto.Argon2
import dev.medzik.libcrypto.Argon2Hash
import dev.medzik.libcrypto.X25519
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.*
import dev.medzik.librepass.utils.Cryptography.computeAesKey
import dev.medzik.librepass.utils.Cryptography.computePasswordHash
import dev.medzik.librepass.utils.Cryptography.computeSharedKey
import dev.medzik.librepass.utils.fromHex
import dev.medzik.librepass.utils.toHex

/**
 * User Client for manage user settings.
 *
 * @param email The email address of the user.
 * @param apiKey The API key to use for authentication.
 * @param apiUrl The API url address (default official production server)
 */
class UserClient(
    private val email: String,
    private val apiKey: String,
    private val apiUrl: String = Server.PRODUCTION
) {
    companion object {
        private const val API_ENDPOINT = "/api/user"
    }

    private val client = Client(apiUrl, apiKey)

    @Throws(ClientException::class, ApiException::class)
    fun changeEmail(
        newEmail: String,
        password: String
    ) {
        val preLogin = AuthClient(apiUrl).preLogin(email)

        val oldPasswordHash =
            computePasswordHash(
                email = email,
                password = password,
                argon2Function = preLogin.toArgon2()
            )
        val newPasswordHash =
            computePasswordHash(
                email = newEmail,
                password = password,
                argon2Function = preLogin.toArgon2()
            )

        val newPublicKey = X25519.publicFromPrivate(newPasswordHash.hash)

        // get server public key
        val serverPublicKey = preLogin.serverPublicKey.fromHex()

        // compute shared key with an old private key and server public key
        val oldSharedKey = computeSharedKey(oldPasswordHash.hash, serverPublicKey)

        // compute shared key with a new private key and server public key
        val newSharedKey = computeSharedKey(newPasswordHash.hash, serverPublicKey)

        val ciphers =
            reEncodeCipher(
                oldPasswordHash,
                newPasswordHash
            )

        val request =
            ChangeEmailRequest(
                newEmail = newEmail,
                oldSharedKey = oldSharedKey.toHex(),
                newSharedKey = newSharedKey.toHex(),
                // X25519 public key
                newPublicKey = newPublicKey.toHex(),
                // ciphers data re-encrypted with new password
                ciphers = ciphers
            )

        client.patch(
            "$API_ENDPOINT/email",
            JsonUtils.serialize(request)
        )
    }

    /**
     * Change user password.
     *
     * @param oldPassword The old password.
     * @param newPassword The new password.
     * @param newPasswordHint The hint for the new password.
     * @param argon2Function The argon2 function for hashing the new password.
     */
    @Throws(ClientException::class, ApiException::class)
    fun changePassword(
        oldPassword: String,
        newPassword: String,
        newPasswordHint: String? = null,
        argon2Function: Argon2 = Argon2(32, 3, 65536, 4)
    ) {
        val oldPreLogin = AuthClient(apiUrl).preLogin(email)

        val oldPasswordHash =
            computePasswordHash(
                email = email,
                password = oldPassword,
                argon2Function = oldPreLogin.toArgon2()
            )

        // compute new password hashes
        val newPasswordHash =
            computePasswordHash(
                email = email,
                password = newPassword,
                argon2Function = argon2Function
            )

        val newPublicKey = X25519.publicFromPrivate(newPasswordHash.hash)

        // get server public key
        val serverPublicKey = oldPreLogin.serverPublicKey.fromHex()

        // compute shared key with an old private key and server public key
        val oldSharedKey = computeSharedKey(oldPasswordHash.hash, serverPublicKey)

        // compute shared key with a new private key and server public key
        val newSharedKey = computeSharedKey(newPasswordHash.hash, serverPublicKey)

        val ciphers =
            reEncodeCipher(
                oldPasswordHash,
                newPasswordHash
            )

        val request =
            ChangePasswordRequest(
                oldSharedKey = oldSharedKey.toHex(),
                newPasswordHint = newPasswordHint,
                newSharedKey = newSharedKey.toHex(),
                // Argon2id parameters
                parallelism = newPasswordHash.parallelism,
                memory = newPasswordHash.memory,
                iterations = newPasswordHash.iterations,
                // X25519 public key
                newPublicKey = newPublicKey.toHex(),
                // ciphers data re-encrypted with new password
                ciphers = ciphers
            )

        client.patch(
            "$API_ENDPOINT/password",
            JsonUtils.serialize(request)
        )
    }

    private fun reEncodeCipher(
        oldPasswordHash: Argon2Hash,
        newPasswordHash: Argon2Hash
    ): List<ChangePasswordCipherData> {
        // compute an old aes key
        val oldAesKey = computeAesKey(oldPasswordHash.hash)

        // compute a new aes key
        val newAesKey = computeAesKey(newPasswordHash.hash)

        // re-encrypt ciphers data with new password
        val cipherClient = CipherClient(apiKey, apiUrl)
        val ciphers = mutableListOf<ChangePasswordCipherData>()
        cipherClient.getAll().forEach { cipher ->
            // decrypt cipher data with an old aes key
            val oldData = Aes.decrypt(Aes.GCM, oldAesKey, cipher.protectedData)

            // encrypt cipher data with a new aes key
            val newData = Aes.encrypt(Aes.GCM, newAesKey, oldData)

            ciphers +=
                ChangePasswordCipherData(
                    id = cipher.id,
                    data = newData
                )
        }

        return ciphers
    }

    /**
     * Setup two-factor authentication for the user.
     *
     * @param password The password of the user.
     * @param secret The OTP secret.
     * @param code The OTP code generated using the [secret].
     */
    @Throws(ClientException::class, ApiException::class)
    fun setupTwoFactor(
        password: String,
        secret: String,
        code: String
    ): SetupTwoFactorResponse {
        val preLogin = AuthClient(apiUrl).preLogin(email)

        val passwordHash =
            computePasswordHash(
                email = email,
                password = password,
                argon2Function = preLogin.toArgon2()
            )

        val serverPublicKey = preLogin.serverPublicKey.fromHex()
        val sharedKey = computeSharedKey(passwordHash.hash, serverPublicKey)

        val request =
            SetupTwoFactorRequest(
                sharedKey = sharedKey.toHex(),
                secret = secret,
                code = code
            )

        val response =
            client.post(
                "$API_ENDPOINT/setup/2fa",
                JsonUtils.serialize(request)
            )
        return JsonUtils.deserialize(response)
    }

    @Throws(ClientException::class, ApiException::class)
    fun deleteAccount(
        password: String,
        tfaCode: String? = null
    ) {
        val preLogin = AuthClient(apiUrl).preLogin(email)

        val passwordHash =
            computePasswordHash(
                email = email,
                password = password,
                argon2Function = preLogin.toArgon2()
            )

        val serverPublicKey = preLogin.serverPublicKey.fromHex()
        val sharedKey = computeSharedKey(passwordHash.hash, serverPublicKey)

        val request =
            DeleteAccountRequest(
                sharedKey = sharedKey.toHex(),
                code = tfaCode
            )

        client.delete(
            "$API_ENDPOINT/delete",
            JsonUtils.serialize(request)
        )
    }
}
