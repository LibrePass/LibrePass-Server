package dev.medzik.librepass.client.api.v1

import net.datafaker.Faker
import org.junit.jupiter.api.Test

class AuthClientTests {
    private val authClient = AuthClient("http://localhost:8080")

    private val email = "_test_" + Faker().internet().emailAddress()
    private val password = Faker().internet().password()

    @Test
    fun register() {
        authClient.register(email, password)
    }

    @Test
    fun login() {
        register() // register user first
        authClient.login(email, password)
    }

    @Test
    fun `refresh token`() {
        register() // register user first
        val credentials = authClient.login(email, password)
        authClient.refresh(credentials.refreshToken)
    }
}
