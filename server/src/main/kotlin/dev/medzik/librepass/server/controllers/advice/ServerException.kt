package dev.medzik.librepass.server.controllers.advice

import dev.medzik.librepass.errors.ServerError

sealed class ServerException(val error: ServerError, val errorMessage: String? = null) : RuntimeException() {
    class CipherNotFound : ServerException(ServerError.CipherNotFound)

    class CollectionNotFound : ServerException(ServerError.CollectionNotFound)

    data class Duplicated(val reason: String) : ServerException(ServerError.Duplicated, reason)

    class EmailInvalidCode : ServerException(ServerError.EmailInvalidCode)

    class EmailNotVerified : ServerException(ServerError.EmailNotVerified)

    data class InvalidBody(val reason: String) : ServerException(ServerError.InvalidBody, reason)

    data class InvalidCipher(val reason: String) : ServerException(ServerError.InvalidCipher, reason)

    data class InvalidCollection(val reason: String) : ServerException(ServerError.InvalidCollection, reason)

    class InvalidSharedKey : ServerException(ServerError.InvalidSharedSecret)

    class InvalidToken : ServerException(ServerError.InvalidToken)

    class InvalidTwoFactor : ServerException(ServerError.InvalidTwoFactor)

    class MissingCipher : ServerException(ServerError.MissingCipher)

    class NotFound : ServerException(ServerError.NotFound)

    class RateLimit : ServerException(ServerError.RateLimit)

    class UserNotFound : ServerException(ServerError.UserNotFound)

    data class Database(val reason: String) : ServerException(ServerError.Database, reason)

    data class Mail(val reason: String) : ServerException(ServerError.Mail, reason)
}
