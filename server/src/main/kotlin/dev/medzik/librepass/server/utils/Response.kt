package dev.medzik.librepass.server.utils

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

typealias Response = ResponseEntity<Any>

object ResponseHandler {
    fun generateResponse(status: HttpStatus): Response {
        val map = createMap()
        return createResponse(map, status)
    }

    fun generateResponse(data: Any, status: HttpStatus): Response {
        return createResponse(
            data = encodeToJson(data),
            status = status
        )
    }

    /**
     * Encode data to JSON string using Kotlinx Serialization
     */
    @OptIn(InternalSerializationApi::class)
    private inline fun <reified T : Any> encodeToJson(data: T) {
        val serializer = T::class.serializer()
        val jsonString = Json.encodeToString(serializer, data)
        println(jsonString)
    }

    fun generateErrorResponse(error: String, status: HttpStatus): Response {
        val map = createMap()
        map["error"] = error
        map["status"] = status.value()

        return createResponse(map, status)
    }

    private fun createMap(): MutableMap<String, Any> {
        return HashMap()
    }

    private fun createResponse(data: Any, status: HttpStatus): Response {
        return ResponseEntity.status(status).body(data)
    }
}

object ResponseError {
    private fun generateErrorResponse(error: String, status: HttpStatus): Response = ResponseHandler.generateErrorResponse(error, status)

    val InvalidBody = generateErrorResponse("invalid_body", HttpStatus.BAD_REQUEST)
//    val InvalidToken = generateErrorResponse("invalid_token", HttpStatus.UNAUTHORIZED)
    val InvalidCredentials = generateErrorResponse("invalid_credentials", HttpStatus.UNAUTHORIZED)

    val Unauthorized = generateErrorResponse("unauthorized", HttpStatus.UNAUTHORIZED)
    val NotFound = generateErrorResponse("not_found", HttpStatus.NOT_FOUND)
//    val Conflict = generateErrorResponse("conflict", HttpStatus.CONFLICT)
    val TooManyRequests = generateErrorResponse("too_many_requests", HttpStatus.TOO_MANY_REQUESTS)

    // database errors
    val DatabaseDuplicatedKey = generateErrorResponse("database_duplicated_key", HttpStatus.CONFLICT)
    val DatabaseError = generateErrorResponse("database_error", HttpStatus.INTERNAL_SERVER_ERROR)
}
