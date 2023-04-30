package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.libcrypto.Salt
import dev.medzik.librepass.types.api.CipherType
import dev.medzik.librepass.types.api.EncryptedCipher
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import dev.medzik.librepass.types.api.auth.UserCredentials
import dev.medzik.librepass.types.api.cipher.InsertResponse
import dev.medzik.librepass.types.api.cipher.SyncResponse
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.datafaker.Faker
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureMockMvc
class CipherControllerTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    private final val urlPrefix = "/api/v1/cipher"

    var email: String = "_test_" + Faker().internet().safeEmailAddress()
    var password: String = Faker().internet().password(true)
    var passwordSalt: ByteArray = Salt.generate(16)

    fun createUser() {
        val request = RegisterRequest(
            email = email,
            // NOTE: This is not how you encrypt passwords in real life
            password = Pbkdf2(100).sha256(password, passwordSalt),
            encryptionKey = Pbkdf2(100).sha256(password, Salt.generate(16)),
            passwordHint = Faker().lorem().characters()
        )

        val json = Json.encodeToString(RegisterRequest.serializer(), request)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
    }

    fun login(): UserCredentials {
        val request = LoginRequest(
            email = email,
            // NOTE: This is not how you encrypt passwords in real life
            password = Pbkdf2(100).sha256(password, passwordSalt)
        )

        val json = Json.encodeToString(LoginRequest.serializer(), request)
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseBody = mvcResult.response.contentAsString
        return Json.decodeFromString(UserCredentials.serializer(), responseBody)
    }

    fun insertCipher(userCredentials: UserCredentials): InsertResponse {
        val request = EncryptedCipher(
            id = UUID.randomUUID(),
            owner = userCredentials.userId,
            type = CipherType.Login.type,
            data = "test"
        )

        val json = Json.encodeToString(EncryptedCipher.serializer(), request)
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.put(urlPrefix)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn()

        val responseBody = mvcResult.response.contentAsString
        return Json.decodeFromString(InsertResponse.serializer(), responseBody)
    }

    fun updateCipher(userCredentials: UserCredentials, cipher: EncryptedCipher): InsertResponse {
        cipher.type = 2
        cipher.data = "test2"
        cipher.favorite = true
        cipher.rePrompt = true

        val json = Json.encodeToString(EncryptedCipher.serializer(), cipher)
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.put(urlPrefix)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andReturn()

        val responseBody = mvcResult.response.contentAsString
        return Json.decodeFromString(InsertResponse.serializer(), responseBody)
    }

    fun listCiphers(userCredentials: UserCredentials): List<EncryptedCipher> {
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get(urlPrefix)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isOk)

        val responseBody = response.andReturn().response.contentAsString
        return Json.decodeFromString(ListSerializer(EncryptedCipher.serializer()), responseBody)
    }

    fun getCipher(userCredentials: UserCredentials, cipherId: String): EncryptedCipher {
        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("$urlPrefix/$cipherId")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseBody = mvcResult.response.contentAsString
        return Json.decodeFromString(EncryptedCipher.serializer(), responseBody)
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

        val ciphers = listCiphers(userCredentials)

        assertEquals(2, ciphers.size)
    }

    @Test
    fun getCipher() {
        val userCredentials = init()
        val insertResponse = insertCipher(userCredentials)
        val cipher = getCipher(userCredentials, insertResponse.id.toString())

        assertEquals(insertResponse.id, cipher.id)
        assertEquals(userCredentials.userId, cipher.owner)

        assertNotNull(cipher.created)
        assertNotNull(cipher.lastModified)
    }

    @Test
    fun syncCiphers() {
        val userCredentials = init()
        insertCipher(userCredentials)

        // wait 1 second to make sure the timestamp is different
        Thread.sleep(1000)

        val timestamp = Date().time / 1000

        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.get("$urlPrefix/sync/$timestamp")
                .header("Authorization", "Bearer ${userCredentials.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseBody = mvcResult.response.contentAsString
        val response = Json.decodeFromString(SyncResponse.serializer(), responseBody)

        assertEquals(1, response.ids.size)
        assert(response.ciphers.isEmpty())
    }

    @Test
    fun deleteCipher() {
        val userCredentials = init()
        val insertResponse = insertCipher(userCredentials)
        deleteCipher(userCredentials, insertResponse.id.toString())
    }
}
