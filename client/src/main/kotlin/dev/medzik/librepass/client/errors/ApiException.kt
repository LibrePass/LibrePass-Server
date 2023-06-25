package dev.medzik.librepass.client.errors

import dev.medzik.librepass.responses.ResponseError

/**
 * Exception thrown when the API returns an error.
 */
class ApiException(
    val status: Number,
    val error: String
) : Exception() {
    override val message: String
        get() = "HTTP $status: $error"

    fun getResponseError(): ResponseError = ResponseError.valueOf(error)
}
