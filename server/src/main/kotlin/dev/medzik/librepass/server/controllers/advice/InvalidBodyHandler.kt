package dev.medzik.librepass.server.controllers.advice

import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@ControllerAdvice
class InvalidBodyHandler {
    @ExceptionHandler(value = [HttpMessageNotReadableException::class])
    fun handleInvalidRequestBody() = invalidBodyResponse("not readable")

    @ExceptionHandler(value = [MissingServletRequestParameterException::class])
    fun handleMissingParameter() = invalidBodyResponse("missing parameter")

    /** Handle request validation exception, `@Valid` annotation */
    @ExceptionHandler(value = [HandlerMethodValidationException::class])
    fun handleValidationException() = invalidBodyResponse("validation error")

    @ExceptionHandler(value = [MethodArgumentTypeMismatchException::class])
    fun handleArgumentTypeMismatch() = invalidBodyResponse("argument type mismatch")

    private fun invalidBodyResponse(reason: String) = makeResponseFromError(ServerException.InvalidBody(reason))
}
