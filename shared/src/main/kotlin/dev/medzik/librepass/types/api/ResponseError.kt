package dev.medzik.librepass.types.api

/**
 * Response that contains API Error.
 *
 * @property error error returned from the server
 * @property status http status code
 * @property message error message returned from the server
 */
data class ResponseError(
    val error: String,
    val status: Int,
    val message: String?
)
