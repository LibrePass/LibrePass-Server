package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.toResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

class AuthorizedUserException : Exception()

/**
 * Handler for @AuthorizedUser annotation.
 */
@ControllerAdvice
class AuthorizedUserErrorHandler {
    @ExceptionHandler(value = [AuthorizedUserException::class])
    fun handleException(): Response {
        return ResponseError.UNAUTHORIZED.toResponse()
    }
}
