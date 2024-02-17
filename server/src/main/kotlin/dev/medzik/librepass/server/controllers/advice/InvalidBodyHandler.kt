package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.errors.InvalidBodyException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class InvalidBodyHandler {
    @ExceptionHandler(value = [HttpMessageNotReadableException::class, MissingServletRequestParameterException::class])
    fun handleInvalidRequestBody() {
        throw InvalidBodyException()
    }
}
