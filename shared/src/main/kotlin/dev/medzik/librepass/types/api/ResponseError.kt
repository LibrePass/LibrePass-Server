package dev.medzik.librepass.types.api

/**
 * API Error response.
 *
 * @property error The error code.
 * @property status The http status.
 * @property message The message of the error.
 */
data class ResponseError(
    val error: String,
    val status: Int,
    val message: String?
)
