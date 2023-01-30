package dev.medzik.vaultbox.server.controllers

import dev.medzik.vaultbox.server.utils.Response
import dev.medzik.vaultbox.server.utils.ResponseError
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalInvalidRequestBodyAdvice {
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidRequestBody(): Response {
        return ResponseError.InvalidBody
    }
}
