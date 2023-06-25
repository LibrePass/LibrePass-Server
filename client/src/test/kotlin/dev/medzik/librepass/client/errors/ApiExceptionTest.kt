package dev.medzik.librepass.client.errors

import dev.medzik.librepass.client.Client
import dev.medzik.librepass.responses.ResponseError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApiExceptionTest {
    private val client = Client("http://localhost:8080")

    @Test
    fun `test invalid body`() {
        try {
            client.post("/api/v1/auth/register", "{}")

            throw Exception("This should cause an exception!")
        } catch (e: ApiException) {
            val responseError = e.getResponseError()

            assertEquals(ResponseError.INVALID_BODY, responseError)
        }
    }
}
