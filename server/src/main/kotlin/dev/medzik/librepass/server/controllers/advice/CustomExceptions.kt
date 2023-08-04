package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.toResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

class AuthorizedUserException : Exception()
class InvalidTwoFactorCodeException : Exception()

@ControllerAdvice
class CustomExceptions {
    @ExceptionHandler(value = [AuthorizedUserException::class])
    fun authorizedUserException(): Response {
        return ResponseError.UNAUTHORIZED.toResponse()
    }

    @ExceptionHandler(value = [InvalidTwoFactorCodeException::class])
    fun invalidTwoFactorCodeException(): Response {
        return ResponseError.INVALID_CREDENTIALS.toResponse()
    }
}
