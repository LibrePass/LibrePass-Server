package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.utils.toResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

class AuthorizedUserException : Exception()
class InvalidTwoFactorCodeException : Exception()
class RateLimitException : Exception()

@ControllerAdvice
class CustomExceptions {
    @ExceptionHandler(value = [AuthorizedUserException::class])
    fun authorizedUserException() = ResponseError.UNAUTHORIZED.toResponse()

    @ExceptionHandler(value = [InvalidTwoFactorCodeException::class])
    fun invalidTwoFactorCodeException() = ResponseError.INVALID_CREDENTIALS.toResponse()

    @ExceptionHandler(value = [RateLimitException::class])
    fun rateLimitException() = ResponseError.TOO_MANY_REQUESTS.toResponse()
}
