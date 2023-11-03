package dev.medzik.librepass.server.utils

import dev.medzik.librepass.responses.ResponseError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

/**
 * Response is a typealias for ResponseEntity<Any>.
 * It is used to simplify the code and make it more readable.
 * The type is used in controllers to return responses.
 */
typealias Response = ResponseEntity<Any>

/** Helper for generating responses. */
object ResponseHandler {
    fun generateResponse(status: HttpStatus) = createResponse(HashMap<String, Any>(), status.value())

    fun generateResponse(
        data: Any,
        status: HttpStatus = HttpStatus.OK
    ) = createResponse(data, status.value())

    fun generateErrorResponse(
        error: String,
        status: Int
    ): Response {
        val map =
            mapOf(
                "error" to error,
                "status" to status
            )

        return createResponse(map, status)
    }

    fun redirectResponse(url: String): Response =
        ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(url))
            .build()

    private fun createResponse(
        data: Any,
        status: Int
    ): Response =
        ResponseEntity
            .status(status)
            .body(data)
}

fun ResponseError.toResponse() = ResponseHandler.generateErrorResponse(this.name, this.statusCode.code)
