package dev.medzik.librepass.types.api.auth

import com.google.gson.Gson
import java.util.*

class RegisterRequest {
    lateinit var email: String
    lateinit var password: String
    var passwordHint: String? = null

    lateinit var encryptionKey: String

    fun toJson(): String {
        return Gson().toJson(this)
    }
}

class LoginRequest {
    lateinit var email: String
    lateinit var password: String

    fun toJson(): String {
        return Gson().toJson(this)
    }
}

class RefreshRequest {
    lateinit var refreshToken: String

    fun toJson(): String {
        return Gson().toJson(this)
    }
}

data class UserCredentials(val userId: UUID, val accessToken: String, val refreshToken: String, val encryptionKey: String)
