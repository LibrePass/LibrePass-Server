package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.utils.toResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

class AuthorizedUserException : RuntimeException()
class InvalidTwoFactorCodeException : RuntimeException()
class RateLimitException : RuntimeException()

@ControllerAdvice
class CustomExceptions {
    @ExceptionHandler(value = [AuthorizedUserException::class])
    fun authorizedUserException() = ResponseError.UNAUTHORIZED.toResponse()

    @ExceptionHandler(value = [InvalidTwoFactorCodeException::class])
    fun invalidTwoFactorCodeException() = ResponseError.INVALID_2FA_CODE.toResponse()

    @ExceptionHandler(value = [RateLimitException::class])
    fun rateLimitException() = ResponseError.TOO_MANY_REQUESTS.toResponse()
}
