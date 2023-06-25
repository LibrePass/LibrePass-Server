package dev.medzik.librepass.types.api

/**
 * API error response.
 * @property error error message returned from the server
 * @property status http status code
 */
data class ResponseError(
    val error: String,
    val status: Int
)
