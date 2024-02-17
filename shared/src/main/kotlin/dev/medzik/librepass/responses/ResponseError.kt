package dev.medzik.librepass.responses

/**
 * HttpStatus represents the HTTP error status codes that can be returned by the API.
 *
 * @property code The HTTP status code.
 */
enum class HttpStatus(val code: Int) {
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    TOO_MANY_REQUESTS(429),
    INTERNAL_SERVER_ERROR(500)
}
