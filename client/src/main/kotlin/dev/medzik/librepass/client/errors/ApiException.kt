package dev.medzik.librepass.client.errors

enum class HttpError {
    BAD_REQUEST,
    UNAUTHORIZED,
    NOT_FOUND,
    TOO_MANY_REQUESTS,
    DUPLICATE,
    SERVER_ERROR,
    UNKNOWN
}

@Suppress("unused")
class ApiException(
    val status: Number,
    val error: String
) : Exception() {
    // print error message
    override val message: String
        get() = "HTTP $status: $error"

    val httpError: HttpError = when (status.toInt()) {
        400 -> HttpError.BAD_REQUEST
        401 -> HttpError.UNAUTHORIZED
        404 -> HttpError.NOT_FOUND
        429 -> HttpError.TOO_MANY_REQUESTS
        409 -> HttpError.DUPLICATE
        500 -> HttpError.SERVER_ERROR
        else -> HttpError.UNKNOWN
    }
}
