package dev.medzik.librepass.types.api

import kotlinx.serialization.Serializable

/**
 * Error response from the API.
 */
@Serializable
data class ResponseError(
    val error: String,
    val status: Int
)
