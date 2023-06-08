package dev.medzik.librepass.server.utils

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * Response is a typealias for ResponseEntity<Any>.
 * It is used to simplify the code and make it more readable.
 * The type is used in controllers to return responses.
 */
typealias Response = ResponseEntity<Any>

/**
 * ResponseHandler is a helper class to generate responses.
 */
object ResponseHandler {
    /**
     * generateResponse generates a response with the given status.
     * The response body is an empty map.
     */
    fun generateResponse(status: HttpStatus): Response {
        val map = createMap()
        return createResponse(map, status)
    }

    /**
     * generateResponse generates a response with the given data that is serialized to JSON.
     * @param data data to serialize
     * @param status status of the response
     * @return The response
     */
    @OptIn(InternalSerializationApi::class)
    inline fun <reified T : Any> generateResponse(data: T, status: HttpStatus): Response {
        val serializer = T::class.serializer()
        val json = Json.encodeToString(serializer, data)

        return ResponseEntity
            .status(status)
            .body(json)
    }

    /**
     * generateResponse generates a response with the given data that is serialized to JSON.
     * @param serializer serializer to use
     * @param data data to serialize
     * @param status status of the response
     * @return The response
     */
    fun <T> generateResponse(serializer: SerializationStrategy<T>, data: T, status: HttpStatus): Response {
        val json = Json.encodeToString(serializer, data)
        return createResponse(json, status)
    }

    /**
     * generateErrorResponse generates an error response with the given error and status.
     * @param error error message
     * @param status status of the response
     * @return The response
     */
    fun generateErrorResponse(error: String, status: HttpStatus): Response {
        val map = createMap()
        map["error"] = error
        map["status"] = status.value()

        return createResponse(map, status)
    }

    private fun createMap(): MutableMap<String, Any> = HashMap()

    private fun createResponse(
        data: Any,
        status: HttpStatus
    ): Response = ResponseEntity
        .status(status)
        .body(data)
}

object ResponseSuccess {
    val OK = ResponseHandler.generateResponse(HttpStatus.OK)
}

/**
 * Utility function to create a error response with the given error and status.
 */
object ResponseError {
    private fun generateErrorResponse(error: String, status: HttpStatus): Response =
        ResponseHandler.generateErrorResponse(error, status)

    val InvalidBody = generateErrorResponse("invalid_body", HttpStatus.BAD_REQUEST)
    val InvalidCredentials = generateErrorResponse("invalid_credentials", HttpStatus.UNAUTHORIZED)

    val Unauthorized = generateErrorResponse("unauthorized", HttpStatus.UNAUTHORIZED)
    val NotFound = generateErrorResponse("not_found", HttpStatus.NOT_FOUND)
    val TooManyRequests = generateErrorResponse("too_many_requests", HttpStatus.TOO_MANY_REQUESTS)

    // database errors
    val DatabaseDuplicatedKey = generateErrorResponse("database_duplicated_key", HttpStatus.CONFLICT)
    val DatabaseError = generateErrorResponse("database_error", HttpStatus.INTERNAL_SERVER_ERROR)
}
