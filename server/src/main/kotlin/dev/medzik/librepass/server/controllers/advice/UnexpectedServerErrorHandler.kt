package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.responses.ResponseError
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.toResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Handler for unexpected exceptions.
 */
@ControllerAdvice
class UnexpectedServerErrorHandler {
    var logger: Logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(value = [Exception::class])
    fun handleException(e: Exception): Response {
        val stackTraceStringWriter = StringWriter()
        e.printStackTrace(PrintWriter(stackTraceStringWriter))
        val stackTrace = stackTraceStringWriter.toString()

        logger.error("Unexpected exception: $stackTrace")

        return ResponseError.UNEXPECTED_SERVER_ERROR.toResponse()
    }
}
