package dev.medzik.librepass.errors

/**
 * ServerError is a set of a possible errors that can occur on the server.
 */
enum class ServerError(val statusCode: HttpStatus, val error: String) {
    CipherNotFound(HttpStatus.NOT_FOUND, "LP-Cipher-404"),
    CollectionNotFound(HttpStatus.NOT_FOUND, "LP-Collection-404"),
    Duplicated(HttpStatus.BAD_REQUEST, "LP-Duplicated"),
    EmailInvalidCode(HttpStatus.BAD_REQUEST, "LP-Email-Invalid-Code"),
    EmailNotVerified(HttpStatus.UNAUTHORIZED, "LP-Email-Not-Verified"),
    InvalidBody(HttpStatus.BAD_REQUEST, "LP-Invalid-Body"),
    InvalidCipher(HttpStatus.BAD_REQUEST, "LP-Invalid-Cipher"),
    InvalidCollection(HttpStatus.BAD_REQUEST, "LP-Invalid-Collection"),
    InvalidSharedSecret(HttpStatus.BAD_REQUEST, "LP-Invalid-Shared-Secret"),
    InvalidToken(HttpStatus.UNAUTHORIZED, "LP-Invalid-Token"),
    InvalidTwoFactor(HttpStatus.BAD_REQUEST, "LP-Invalid-Two-Factor"),
    MissingCipher(HttpStatus.BAD_REQUEST, "LP-Missing-Cipher"),
    NotFound(HttpStatus.NOT_FOUND, "LP-404"),
    RateLimit(HttpStatus.TOO_MANY_REQUESTS, "LP-RateLimit"),
    UserNotFound(HttpStatus.BAD_REQUEST, "LP-User-404"),
    Database(HttpStatus.INTERNAL_SERVER_ERROR, "LP-Database-Error"),
    Mail(HttpStatus.INTERNAL_SERVER_ERROR, "LP-Mail-Error")
}
