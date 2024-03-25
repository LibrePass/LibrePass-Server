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
 * @param email user's email
 * @param apiKey user's api key
 * @param apiUrl server api url (default: official production server)
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

    /**
     * Changes user's email address.
     *
     * @param newEmail new user email address
     * @param password user's password
     */
    @Throws(ClientException::class, ApiException::class)
    fun changeEmail(
        newEmail: String,
        password: String
    ) {
        val preLogin = AuthClient(apiUrl).preLogin(email)

        // computes hash using old email address as salt
        val oldPasswordHash = computePasswordHash(
            email = email,
            password = password,
            argon2Function = preLogin.toArgon2()
        )
        // computes hash using new email address as salt
        val newPasswordHash = computePasswordHash(
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

        val ciphers = reEncodeCipher(
            oldPasswordHash,
            newPasswordHash
        )

        val request = ChangeEmailRequest(
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
     * Changes user password.
     *
     * @param oldPassword user's old password
     * @param newPassword user's new password
     * @param newPasswordHint hint for the new password
     * @param argon2Function argon2 function for computing password hash
     */
    @Throws(ClientException::class, ApiException::class)
    fun changePassword(
        oldPassword: String,
        newPassword: String,
        newPasswordHint: String? = null,
        argon2Function: Argon2 = Argon2(32, 3, 65536, 4)
    ) {
        // get pre-login data for the old password
        val oldPreLogin = AuthClient(apiUrl).preLogin(email)

        // compute password hash for the old password
        val oldPasswordHash = computePasswordHash(
            email = email,
            password = oldPassword,
            argon2Function = oldPreLogin.toArgon2()
        )

        // compute password hash for the new password
        val newPasswordHash = computePasswordHash(
            email = email,
            password = newPassword,
            argon2Function = argon2Function
        )
        // compute new public key
        val newPublicKey = X25519.publicFromPrivate(newPasswordHash.hash)

        // get server public key
        val serverPublicKey = oldPreLogin.serverPublicKey.fromHex()

        // compute shared key with an old private key and server public key
        val oldSharedKey = computeSharedKey(oldPasswordHash.hash, serverPublicKey)
        // compute shared key with a new private key and server public key
        val newSharedKey = computeSharedKey(newPasswordHash.hash, serverPublicKey)

        // re-encode ciphers due to new encryption key
        val ciphers = reEncodeCipher(
            oldPasswordHash,
            newPasswordHash
        )

        val request = ChangePasswordRequest(
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

        // re-encrypt ciphers data with new encryption key
        val cipherClient = CipherClient(apiKey, apiUrl)
        val ciphers = mutableListOf<ChangePasswordCipherData>()
        cipherClient.getAll().forEach { cipher ->
            // decrypt cipher data with an old aes key
            val oldData = Aes.decrypt(Aes.GCM, oldAesKey, cipher.protectedData)

            // encrypt cipher data with a new aes key
            val newData = Aes.encrypt(Aes.GCM, newAesKey, oldData)

            ciphers += ChangePasswordCipherData(
                id = cipher.id,
                data = newData
            )
        }

        return ciphers
    }

    /**
     * Sets up two-factor authentication for the user's account.
     *
     * @param password user's password
     * @param secret totp secret
     * @param code one-time password generated using the [secret] to verify it
     * @return [SetupTwoFactorResponse]
     */
    @Throws(ClientException::class, ApiException::class)
    fun setupTwoFactor(
        password: String,
        secret: String,
        code: String
    ): SetupTwoFactorResponse {
        val preLogin = AuthClient(apiUrl).preLogin(email)

        val passwordHash = computePasswordHash(
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

        val response = client.post(
            "$API_ENDPOINT/setup/2fa",
            JsonUtils.serialize(request)
        )
        return JsonUtils.deserialize(response)
    }

    /**
     * Deletes the user's account.
     *
     * @param password the user's password
     * @param tfaCode the user's two-factor authentication code (optional, only
     * if the user has two-factor authentication enabled)
     */
    @Throws(ClientException::class, ApiException::class)
    fun deleteAccount(
        password: String,
        tfaCode: String? = null
    ) {
        val preLogin = AuthClient(apiUrl).preLogin(email)

        val passwordHash = computePasswordHash(
            email = email,
            password = password,
            argon2Function = preLogin.toArgon2()
        )

        val serverPublicKey = preLogin.serverPublicKey.fromHex()
        val sharedKey = computeSharedKey(passwordHash.hash, serverPublicKey)

        val request = DeleteAccountRequest(
            sharedKey = sharedKey.toHex(),
            code = tfaCode
        )

        client.delete(
            "$API_ENDPOINT/delete",
            JsonUtils.serialize(request)
        )
    }
}
