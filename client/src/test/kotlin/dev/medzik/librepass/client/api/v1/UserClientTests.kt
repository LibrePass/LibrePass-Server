package dev.medzik.librepass.client.api.v1

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserClientTests {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            val authClient = AuthClient("http://localhost:8080")
            authClient.register("test_user@example.com", "test")
        }
    }

    lateinit var userClient: UserClient

    @BeforeEach
    fun beforeEach() {
        val authClient = AuthClient("http://localhost:8080")
        val credentials = authClient.login("test_user@example.com", "test")

        userClient = UserClient("test_user@example.com", credentials.accessToken, "http://localhost:8080")
    }

    @Test
    fun changePassword() {
        userClient.changePassword("test", "test2")

        // login with new password
        val authClient = AuthClient("http://localhost:8080")
        authClient.login("test_user@example.com", "test2")

        // change password back
        userClient.changePassword("test2", "test")
    }

    @Test
    fun getSecrets() {
        userClient.getSecrets("test")
    }
}
