package dev.medzik.librepass.types.api

import kotlinx.serialization.Serializable

/**
 * Error response from the API. Contains the error message and the status code.
 * @property error The error message.
 * @property status The status code.
 */
@Serializable
data class ResponseError(
    val error: String,
    val status: Int
)
