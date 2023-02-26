package dev.medzik.librepass.client.api.v1

import com.google.gson.Gson
import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RefreshRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserCredentials
import java.io.IOException

const val EncryptionKeyIterations = 5000 // 5k iterations
const val PasswordIterations = 100000 // 100k iterations

class Auth(apiUrl: String = Client.DefaultApiUrl) {
    private val apiEndpoint = "/api/v1/auth"

    private val client = Client(null, apiUrl)

    @Throws(IOException::class)
    fun register(email: String, password: String) {
        register(email, password, null)
    }

    @Throws(IOException::class)
    fun register(email: String, password: String, passwordHint: String?) {
        val finalPassword = Pbkdf2(PasswordIterations).sha256(password, email.encodeToByteArray())
        val encryptionKey = Pbkdf2(EncryptionKeyIterations).sha256(password, ByteArray(16))

        val request = RegisterRequest()
        request.email = email
        request.password = finalPassword
        request.passwordHint = passwordHint
        request.encryptionKey = encryptionKey

        client.post("$apiEndpoint/register", request.toJson())
    }

    @Throws(IOException::class)
    fun login(email: String, password: String): UserCredentials {
        val finalPassword = Pbkdf2(PasswordIterations).sha256(password, email.encodeToByteArray())

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
