package dev.medzik.librepass.server.utils

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * Response is a typealias for ResponseEntity<Any>.
 * It is used to simplify the code and make it more readable.
 * The type is used in controllers to return responses.
 */
typealias Response = ResponseEntity<Any>

/**
 * ResponseHandler is a helper class for generating responses.
 */
object ResponseHandler {
    /**
     * Generates a response with the given status.
     * The response body is an empty map.
     */
    fun generateResponse(status: HttpStatus) =
        createResponse(HashMap<String, Any>(), status)

    /**
     * Generates a response with the given data that is serialized to JSON.
     * @param data data to serialize
     * @param status status of the response
     * @return The response
     */
    fun generateResponse(data: Any, status: HttpStatus) =
        createResponse(data, status)

    /**
     * Generates an error response with the given error and status.
     * @param error error message
     * @param status status of the response
     * @return The response
     */
    fun generateErrorResponse(error: String, status: HttpStatus): Response {
        val map = mapOf(
            "error" to error,
            "status" to status.value()
        )

        return createResponse(map, status)
    }

    private fun createResponse(
        data: Any,
        status: HttpStatus
    ): Response = ResponseEntity
        .status(status)
        .body(data)
}

/**
 * ResponseSuccess is a helper class for generating successful responses.
 */
object ResponseSuccess {
    val OK = ResponseHandler.generateResponse(HttpStatus.OK)
}

/**
 * ResponseHandler is a helper class for generating error responses.
 */
object ResponseError {
    private fun generateErrorResponse(error: String, status: HttpStatus): Response =
        ResponseHandler.generateErrorResponse(error, status)

    val InvalidBody = generateErrorResponse("invalid_body", HttpStatus.BAD_REQUEST)
    val InvalidCredentials = generateErrorResponse("invalid_credentials", HttpStatus.UNAUTHORIZED)
    val EmailNotVerified = generateErrorResponse("email_not_verified", HttpStatus.UNAUTHORIZED)

    val Unauthorized = generateErrorResponse("unauthorized", HttpStatus.UNAUTHORIZED)
    val NotFound = generateErrorResponse("not_found", HttpStatus.NOT_FOUND)
    val TooManyRequests = generateErrorResponse("too_many_requests", HttpStatus.TOO_MANY_REQUESTS)

    // database errors
    val DatabaseDuplicatedKey = generateErrorResponse("database_duplicated_key", HttpStatus.CONFLICT)
    val DatabaseError = generateErrorResponse("database_error", HttpStatus.INTERNAL_SERVER_ERROR)
}
