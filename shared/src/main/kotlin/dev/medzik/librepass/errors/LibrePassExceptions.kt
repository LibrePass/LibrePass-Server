package dev.medzik.librepass.errors

import dev.medzik.librepass.responses.HttpStatus

enum class LibrePassExceptions(val statusCode: HttpStatus) {
    CipherNotFound(HttpStatus.NOT_FOUND),
    CollectionNotFound(HttpStatus.NOT_FOUND),
    Database(HttpStatus.INTERNAL_SERVER_ERROR),
    Duplicated(HttpStatus.BAD_REQUEST),
    EmailInvalidCode(HttpStatus.BAD_REQUEST),
    EmailNotVerified(HttpStatus.UNAUTHORIZED),
    InvalidBody(HttpStatus.BAD_REQUEST),
    InvalidCipher(HttpStatus.BAD_REQUEST),
    InvalidCollection(HttpStatus.BAD_REQUEST),
    InvalidSharedSecret(HttpStatus.BAD_REQUEST),
    InvalidToken(HttpStatus.UNAUTHORIZED),
    InvalidTwoFactor(HttpStatus.BAD_REQUEST),
    MissingCipher(HttpStatus.BAD_REQUEST),
    NotFound(HttpStatus.NOT_FOUND),
    RateLimit(HttpStatus.TOO_MANY_REQUESTS),
    SendEmail(HttpStatus.INTERNAL_SERVER_ERROR),
    UserNotFound(HttpStatus.BAD_REQUEST)
}
