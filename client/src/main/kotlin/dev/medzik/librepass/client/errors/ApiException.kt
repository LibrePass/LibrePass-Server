package dev.medzik.librepass.client.errors

import dev.medzik.librepass.responses.ResponseError

/**
 * Exception thrown when the API returns an error.
 *
 * @property status The HTTP Status code returned by the API.
 * @property error The error message returned by the API.
 */
class ApiException(
    val status: Number,
    val error: String
) : Exception() {
    override val message: String = "HTTP $status: $error"

    @Suppress("UNUSED")
    val responseError = ResponseError.valueOf(error)
}
