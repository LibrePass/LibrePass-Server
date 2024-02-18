package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.errors.BaseException
import dev.medzik.librepass.errors.ServerError
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseHandler
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

fun makeResponseFromError(error: ServerError): Response {
    return ResponseHandler.generateErrorResponse(
        error = error.name,
        status = error.statusCode.code
    )
}

@ControllerAdvice
class CustomExceptions {
    @ExceptionHandler(value = [BaseException::class])
    fun authorizedUserException(e: BaseException) = makeResponseFromError(e.error)
}
