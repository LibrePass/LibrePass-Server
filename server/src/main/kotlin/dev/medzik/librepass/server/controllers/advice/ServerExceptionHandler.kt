package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

fun makeResponseFromError(error: ServerException): Response {
    return ResponseHandler.generateErrorResponse(
        error = error.error.error,
        status = error.error.statusCode.code,
        message = error.errorMessage
    )
}

@ControllerAdvice
class ServerExceptionHandler {
    @ExceptionHandler(value = [ServerException::class])
    fun authorizedUserException(e: ServerException) = makeResponseFromError(e)
}
