package dev.medzik.librepass.client.api.v1

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AuthClientTests {
    private val authClient = AuthClient("http://localhost:8080")

    @Test
    @Order(1)
    fun register() {
        authClient.register("test", "test")
    }

    @Test
    @Order(2)
    fun login() {
        authClient.login("test", "test")
    }

    @Test
    @Order(3)
    fun refresh() {
        val credentials = authClient.login("test", "test")
        authClient.refresh(credentials.refreshToken)
    }
}
