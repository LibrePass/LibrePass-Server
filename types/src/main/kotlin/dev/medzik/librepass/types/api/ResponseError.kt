package dev.medzik.librepass.types.api

import kotlinx.serialization.Serializable

/**
 * API error response.
 * @property error error message returned from the server
 * @property status http status code
 */
@Serializable
data class ResponseError(
    val error: String,
    val status: Int
)
