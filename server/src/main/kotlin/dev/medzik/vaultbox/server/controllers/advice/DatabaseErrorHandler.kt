package dev.medzik.vaultbox.server.controllers.advice

import dev.medzik.vaultbox.server.utils.Response
import dev.medzik.vaultbox.server.utils.ResponseError
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
            // duplicated key
            if (cve!!.sqlState == "23505") {
                return ResponseError.DatabaseDuplicatedKey
            }
        }

        return ResponseError.DatabaseError
    }

    @ExceptionHandler(value = [UninitializedPropertyAccessException::class])
    fun handleUninitializedPropertyError(e: UninitializedPropertyAccessException): Response {
        return ResponseError.InvalidBody
    }
}
