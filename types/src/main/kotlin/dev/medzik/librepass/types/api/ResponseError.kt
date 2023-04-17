package dev.medzik.librepass.types.api

import kotlinx.serialization.Serializable

@Serializable
data class ResponseError(
    val error: String,
    val status: Int
)
