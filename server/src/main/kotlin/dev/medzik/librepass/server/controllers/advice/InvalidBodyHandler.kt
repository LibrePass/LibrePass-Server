package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.utils.toResponse
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Handler for invalid body parameters.
 */
@ControllerAdvice
class InvalidBodyHandler {
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidRequestBody() = ResponseError.INVALID_BODY.toResponse()

    @ExceptionHandler(NullPointerException::class)
    fun handleException() = ResponseError.INVALID_BODY.toResponse()
}
