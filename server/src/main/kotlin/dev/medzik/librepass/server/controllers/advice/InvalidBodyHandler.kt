package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseError
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Handler for invalid body parameters.
 */
@ControllerAdvice
class InvalidBodyHandler {
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidRequestBody(): Response {
        return ResponseError.InvalidBody
    }
}
