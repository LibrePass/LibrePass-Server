package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.utils.toResponse
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class NotFoundHandler {
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleInvalidRequestBody() = ResponseError.NOT_FOUND.toResponse()
}
