package dev.medzik.vaultbox.types.api.auth

import java.util.*

class RegisterRequest {
    lateinit var email: String
    lateinit var password: String
    var passwordHint: String? = null

    lateinit var encryptionKey: String
}

class LoginRequest {
    lateinit var email: String
    lateinit var password: String
}

class RefreshRequest {
    lateinit var refreshToken: String
}

class UserCredentials(val userId: UUID, val accessToken: String, val refreshToken: String)
