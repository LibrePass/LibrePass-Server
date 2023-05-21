package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseError
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Handle database errors.
 */
@ControllerAdvice
class DatabaseErrorHandler {
    @ExceptionHandler(value = [DataIntegrityViolationException::class])
    fun handleDatabaseError(e: DataIntegrityViolationException): Response {
        if (e.cause is ConstraintViolationException) {
            val cve = e.cause as ConstraintViolationException?
            // duplicate key
            if (cve!!.sqlState == "23505") {
                return ResponseError.DatabaseDuplicatedKey
            }
        }

        // unknown database error
        return ResponseError.DatabaseError
    }

    @ExceptionHandler(value = [UninitializedPropertyAccessException::class])
    fun handleUninitializedPropertyError(e: UninitializedPropertyAccessException): Response {
        return ResponseError.InvalidBody
    }
}
