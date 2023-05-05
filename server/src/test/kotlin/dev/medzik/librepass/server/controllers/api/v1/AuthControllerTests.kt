package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserArgon2idParameters
import kotlinx.serialization.json.Json
import net.datafaker.Faker
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private final val urlPrefix = "/api/v1/auth"

    var email: String = "_test_" + Faker().internet().safeEmailAddress()
    var password: String = Faker().internet().password(true)
    var passwordSalt: ByteArray = Salt.generate(16)

    fun createUser() {
        val request = RegisterRequest(
            email = email,
            // NOTE: This is not how you encrypt passwords in real life
            password = Pbkdf2(100).sha256(password, passwordSalt),
            encryptionKey = Pbkdf2(100).sha256(password, Salt.generate(16)),
            passwordHint = Faker().lorem().characters(),
            // test data
            parallelism = 3,
            memory = 65536,
            iterations = 4,
            version = 19,
            // rsa keypair
            // NOTE: These tests do not need this
            publicKey = "",
            privateKey = ""
        )

        val json = Json.encodeToString(RegisterRequest.serializer(), request)
        mockMvc.perform(
            post("${urlPrefix}/register")
                .contentType(MediaType.APPLICATION_JSON).content(json)
        ).andExpect(status().isCreated)
    }

    fun login(expect: ResultMatcher) {
        val request = LoginRequest(
            email = email,
            // NOTE: This is not how you encrypt passwords in real life
            password = Pbkdf2(100).sha256(password, passwordSalt)
        )

        val json = Json.encodeToString(LoginRequest.serializer(), request)
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
    fun `register and check argon2id settings`() {
        createUser()

        val json = mockMvc.perform(get("${urlPrefix}/userArgon2Parameters?=email=${email}"))
        val response = Json.decodeFromString(UserArgon2idParameters.serializer(), json.andReturn().response.contentAsString)

        assert(response.parallelism == 3)
        assert(response.memory == 65536)
        assert(response.iterations == 4)
        assert(response.version == 19)
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
