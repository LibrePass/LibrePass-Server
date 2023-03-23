package dev.medzik.librepass.client.api.v1

import com.google.gson.Gson
import dev.medzik.libcrypto.AesCbc
import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RefreshRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserCredentials
import org.apache.commons.codec.binary.Hex
import java.io.IOException

const val EncryptionKeyIterations = 500 // 500 iterations
const val PasswordIterations = 65000 // 65k iterations

class AuthClient(apiUrl: String = Client.DefaultApiUrl) {
    private val apiEndpoint = "/api/v1/auth"

    private val client = Client(null, apiUrl)

    @Throws(IOException::class)
    fun register(email: String, password: String) {
        register(email, password, null)
    }

    @Throws(IOException::class)
    fun register(email: String, password: String, passwordHint: String?) {
        // compute the PBKDF2 sha256 hash of the password with 65k iterations and with email as salt
        val basePassword = Pbkdf2(PasswordIterations).sha256(password, email.encodeToByteArray())
        // compute the final password, it is required since the earlier hash is used to encrypt the encryption key
        val finalPassword = Pbkdf2(EncryptionKeyIterations).sha256(basePassword, email.encodeToByteArray())

        // create a random byte array with 16 bytes and encode it to hex string,
        // then compute the PBKDF2 sha256 hash with 500 iterations and with random salt of 16 bytes length
        val encryptionKeyBase = Pbkdf2(EncryptionKeyIterations).sha256(Hex.encodeHexString(Salt.generate(16)), Salt.generate(16))
        val encryptionKey = AesCbc.encrypt(encryptionKeyBase, basePassword)

        val request = RegisterRequest()
        request.email = email
        request.password = finalPassword
        request.passwordHint = passwordHint
        request.encryptionKey = encryptionKey

        client.post("$apiEndpoint/register", request.toJson())
    }

    @Throws(IOException::class)
    fun login(email: String, password: String): UserCredentials {
        val basePassword = Pbkdf2(PasswordIterations).sha256(password, email.encodeToByteArray())
        val finalPassword = Pbkdf2(EncryptionKeyIterations).sha256(basePassword, email.encodeToByteArray())

        val request = LoginRequest()
        request.email = email
        request.password = finalPassword

        val body = client.post("$apiEndpoint/login", request.toJson())

        return Gson().fromJson(body, UserCredentials::class.java)
    }

    @Throws(IOException::class)
    fun refresh(refreshToken: String): UserCredentials {
        val request = RefreshRequest()
        request.refreshToken = refreshToken

        val body = client.post("$apiEndpoint/refresh", request.toJson())

        return Gson().fromJson(body, UserCredentials::class.java)
    }
}
