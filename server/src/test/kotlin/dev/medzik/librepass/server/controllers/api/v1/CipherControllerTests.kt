package dev.medzik.librepass.server.controllers.api.v1

import com.github.javafaker.Faker
import com.google.gson.Gson
import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.types.api.EncryptedCipher
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserCredentials
import dev.medzik.librepass.types.api.cipher.InsertResponse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
class CipherControllerTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    private final val urlPrefix = "/api/v1/cipher"

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
            MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
    }

    fun login(): UserCredentials {
        val request = LoginRequest()
        request.email = email
        request.password = Pbkdf2(100).sha256(password, passwordSalt)

        val json = Gson().toJson(request)
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseBody = mvcResult.response.contentAsString
        return Gson().fromJson(responseBody, UserCredentials::class.java)
    }

    fun insertCipher(userCredentials: UserCredentials): InsertResponse {
        val request = EncryptedCipher()
        request.id = UUID.randomUUID()
        request.owner = userCredentials.userId
        request.type = 1
        request.data = "test"

        val json = Gson().toJson(request)
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.put(urlPrefix)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn()

        val responseBody = mvcResult.response.contentAsString
        return Gson().fromJson(responseBody, InsertResponse::class.java)
    }

    fun updateCipher(userCredentials: UserCredentials, cipher: EncryptedCipher): InsertResponse {
        cipher.type = 2
        cipher.data = "test2"
        cipher.favorite = true
        cipher.rePrompt = true

        val json = Gson().toJson(cipher)
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.put(urlPrefix)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn()

        val responseBody = mvcResult.response.contentAsString
        return Gson().fromJson(responseBody, InsertResponse::class.java)
    }

    fun listCiphers(userCredentials: UserCredentials) {
        mockMvc.perform(
            MockMvcRequestBuilders.get(urlPrefix)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    fun getCipher(userCredentials: UserCredentials, cipherId: String): EncryptedCipher {
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("$urlPrefix/$cipherId")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseBody = mvcResult.response.contentAsString
        return Gson().fromJson(responseBody, EncryptedCipher::class.java)
    }

    fun deleteCipher(userCredentials: UserCredentials, cipherId: String) {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("$urlPrefix/$cipherId")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    fun init(): UserCredentials {
        createUser()
        return login()
    }

    @Test
    fun insertCipher() {
        val userCredentials = init()
        insertCipher(userCredentials)
    }

    @Test
    fun updateCipher() {
        val userCredentials = init()
        val insertResponse = insertCipher(userCredentials)
        val cipher = getCipher(userCredentials, insertResponse.id.toString())
        updateCipher(userCredentials, cipher)
    }

    @Test
    fun listCiphers() {
        val userCredentials = init()
        insertCipher(userCredentials)
        insertCipher(userCredentials)
        listCiphers(userCredentials)
    }

    @Test
    fun getCipher() {
        val userCredentials = init()
        val insertResponse = insertCipher(userCredentials)
        val cipher = getCipher(userCredentials, insertResponse.id.toString())

        assert(cipher.id == insertResponse.id)
        assert(cipher.owner == userCredentials.userId)
    }

    @Test
    fun deleteCipher() {
        val userCredentials = init()
        val insertResponse = insertCipher(userCredentials)
        deleteCipher(userCredentials, insertResponse.id.toString())
    }
}
