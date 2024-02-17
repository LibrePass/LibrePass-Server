package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.errors.DatabaseException
import dev.medzik.librepass.errors.DuplicatedException
import dev.medzik.librepass.server.utils.Response
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
                throw DuplicatedException()
            }
        }

        // unknown database error
        throw DatabaseException()
    }

//    @ExceptionHandler(value = [UninitializedPropertyAccessException::class])
//    fun handleUninitializedPropertyError(e: UninitializedPropertyAccessException): Response {
//        return ResponseError.INVALID_BODY.toResponse()
//    }
}
