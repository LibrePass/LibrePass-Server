package dev.medzik.librepass.server.controllers.advice

import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.HandlerMethodValidationException

@ControllerAdvice
class InvalidBodyHandler {
    @ExceptionHandler(value = [HttpMessageNotReadableException::class])
    fun handleInvalidRequestBody() = invalidBodyResponse("not readable")

    @ExceptionHandler(value = [MissingServletRequestParameterException::class])
    fun handleMissingParameter() = invalidBodyResponse("missing parameter")

    /** Handle request validation exception, `@Valid` annotation */
    @ExceptionHandler(value = [HandlerMethodValidationException::class])
    fun handleValidationException() = invalidBodyResponse("validation error")

    private fun invalidBodyResponse(reason: String) = makeResponseFromError(ServerException.InvalidBody(reason))
}
