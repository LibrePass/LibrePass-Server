package dev.medzik.librepass.client.api.utils

import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.errors.ServerError
import org.junit.jupiter.api.Assertions

fun assertApiError(error: ServerError, scope: () -> Unit) {
    try {
        scope()
    } catch (e: ApiException) {
        Assertions.assertEquals(error, e.getServerError())
    }
}
