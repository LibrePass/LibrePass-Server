package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.errors.LibrePassException
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class CustomExceptions {
    @ExceptionHandler(value = [LibrePassException::class])
    fun authorizedUserException(e: LibrePassException): Response {
        return ResponseHandler.generateErrorResponse(
            error = e.enum.name,
            status = e.enum.statusCode.code
        )
    }
}
