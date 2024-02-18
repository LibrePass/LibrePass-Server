package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.errors.ServerError
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.HandlerMethodValidationException

@ControllerAdvice
class InvalidBodyHandler {
    @ExceptionHandler(value = [HttpMessageNotReadableException::class, MissingServletRequestParameterException::class])
    fun handleInvalidRequestBody() = invalidBodyResponse()

    /** Handle request validation exception, `@Valid` annotation */
    @ExceptionHandler(value = [HandlerMethodValidationException::class])
    fun validationException() = invalidBodyResponse()

    private fun invalidBodyResponse() = makeResponseFromError(ServerError.InvalidBody)
}
