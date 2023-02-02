package dev.medzik.vaultbox.server.controllers.api.v1

import com.github.javafaker.Faker
import com.google.gson.Gson
import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.vaultbox.types.api.auth.LoginRequest
import dev.medzik.vaultbox.types.api.auth.RegisterRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private final val urlPrefix = "/api/v1/auth"

    var email: String = Faker().internet().safeEmailAddress()
    var password: String = Faker().internet().password(true)
    var passwordSalt: ByteArray = Salt.generate(16)

    fun createUser() {
        val request = RegisterRequest()
        request.email = email
        request.password = Pbkdf2(100).sha256(password, passwordSalt)
        request.encryptionKey = Pbkdf2(100).sha256(password, Salt.generate(16))
        request.passwordHint = Faker().lorem().characters()

        val json = Gson().toJson(request)
        mockMvc.perform(
            post("${urlPrefix}/register")
                .contentType(MediaType.APPLICATION_JSON).content(json)
        ).andExpect(status().isCreated)
    }

    fun login(expect: ResultMatcher) {
        val request = LoginRequest()
        request.email = email
        request.password = Pbkdf2(100).sha256(password, passwordSalt)

        val json = Gson().toJson(request)
        mockMvc.perform(
            post("${urlPrefix}/login")
                .contentType(MediaType.APPLICATION_JSON).content(json)
        ).andExpect(expect)
    }

    @Test
    fun `register and login`() {
        createUser()
        login(status().isOk)
    }

    @Test
    fun `register and login with wrong password`() {
        createUser()
        password += "invalid password"
        login(status().isUnauthorized)
    }

    @Test
    fun `register and login with wrong email`() {
        createUser()
        email += "invalid email"
        login(status().isUnauthorized)
    }
}
