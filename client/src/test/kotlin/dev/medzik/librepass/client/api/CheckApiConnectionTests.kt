package dev.medzik.librepass.client.api

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CheckApiConnectionTests {
    @Test
    fun checkApiConnection() {
        assertTrue(checkApiConnection(API_URL))
    }
}
