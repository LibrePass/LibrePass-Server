package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.toResponse
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class DatabaseErrorHandler {
    @ExceptionHandler(value = [DataIntegrityViolationException::class])
    fun handleDatabaseError(e: DataIntegrityViolationException): Response {
        if (e.cause is ConstraintViolationException) {
            val cve = e.cause as ConstraintViolationException?

            // duplicate key
            if (cve?.sqlState == "23505") {
                return ResponseError.DATABASE_DUPLICATED_KEY.toResponse()
            }
        }

        // unknown database error
        return ResponseError.DATABASE_ERROR.toResponse()
    }

    @ExceptionHandler(value = [UninitializedPropertyAccessException::class])
    fun handleUninitializedPropertyError(e: UninitializedPropertyAccessException): Response {
        return ResponseError.INVALID_BODY.toResponse()
    }
}
