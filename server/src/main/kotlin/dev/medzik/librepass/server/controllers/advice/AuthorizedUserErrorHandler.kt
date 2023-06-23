package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseError
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
        return ResponseError.InvalidCredentials
    }
}
