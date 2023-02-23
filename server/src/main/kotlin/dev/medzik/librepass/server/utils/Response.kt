package dev.medzik.librepass.server.utils

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

typealias Response = ResponseEntity<Any>

object ResponseHandler {
    fun generateResponse(status: HttpStatus): Response {
        val map = createMap()
        map["status"] = status.value()

        return createResponse(map)
    }

    fun generateResponse(data: Any, status: HttpStatus): Response {
        val map = createMap()
        map["data"] = data
        map["status"] = status.value()

        return createResponse(map)
    }

    fun generateErrorResponse(error: String, status: HttpStatus): Response {
        val map = createMap()
        map["code"] = error
        map["status"] = status.value()

        return createResponse(map)
    }

    private fun createMap(): MutableMap<String, Any> {
        return HashMap()
    }

    private fun createResponse(map: Map<String, Any>): Response {
        val status = map["status"] as Int
        return ResponseEntity.status(status).body(map)
    }
}

object ResponseError {
    private fun generateErrorResponse(error: String, status: HttpStatus): Response = ResponseHandler.generateErrorResponse(error, status)

    val InvalidBody = generateErrorResponse("invalid_body", HttpStatus.BAD_REQUEST)
    val InvalidToken = generateErrorResponse("invalid_token", HttpStatus.UNAUTHORIZED)
    val InvalidCredentials = generateErrorResponse("invalid_credentials", HttpStatus.UNAUTHORIZED)

    val Unauthorized = generateErrorResponse("unauthorized", HttpStatus.UNAUTHORIZED)
    val NotFound = generateErrorResponse("not_found", HttpStatus.NOT_FOUND)
    val Conflict = generateErrorResponse("conflict", HttpStatus.CONFLICT)

    // database errors
    val DatabaseDuplicatedKey = generateErrorResponse("database_duplicated_key", HttpStatus.CONFLICT)
    val DatabaseError = generateErrorResponse("database_error", HttpStatus.INTERNAL_SERVER_ERROR)
}
