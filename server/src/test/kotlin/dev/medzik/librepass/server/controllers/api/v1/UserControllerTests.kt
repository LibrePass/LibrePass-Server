package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.libcrypto.Pbkdf2
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.user.ChangePasswordRequest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private final val authControllerPrefix = "/api/v1/auth"
    private final val userControllerPrefix = "/api/v1/user"

    @Test
    fun `create user, login and change password`() {
        val authController = AuthControllerTests()
        authController.mockMvc = mockMvc

        authController.createUser()
        val credentials = authController.login(MockMvcResultMatchers.status().isOk)

        // change password
        val changePasswordRequest = ChangePasswordRequest(
            // NOTE: This is not how you encrypt passwords in real life
            oldPassword = Pbkdf2(100).sha256(authController.password, authController.passwordSalt),
            newPassword = Pbkdf2(300).sha256(authController.password, authController.passwordSalt),
            newEncryptionKey = Pbkdf2(200).sha256(authController.password, authController.passwordSalt),
            parallelism = 3,
            memory = 65536,
            iterations = 4,
            version = 19
        )

        // change password
        val changePasswordJson = Json.encodeToString(ChangePasswordRequest.serializer(), changePasswordRequest)
        mockMvc.perform(
            MockMvcRequestBuilders.patch("${userControllerPrefix}/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(changePasswordJson)
                .header("Authorization", "Bearer ${credentials!!.accessToken}")
        ).andExpect(MockMvcResultMatchers.status().isOk)

        // login
        val loginRequest = LoginRequest(
            email = authController.email,
            // NOTE: This is not how you encrypt passwords in real life
            password = Pbkdf2(300).sha256(authController.password, authController.passwordSalt)
        )

        val loginRequestJson = Json.encodeToString(LoginRequest.serializer(), loginRequest)
        mockMvc.perform(
            MockMvcRequestBuilders.post("${authControllerPrefix}/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequestJson)
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }
}
