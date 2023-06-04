package dev.medzik.librepass.client.errors

import java.io.IOException

/**
 * Exception thrown when a client error occurred while executing a request. This is not thrown when the server returns
 * an error response.
 */
class ClientException(private val ioException: IOException) : Exception() {
    override val message: String
        get() = "Client error occurred while executing request ${ioException.message}"

    override val cause: Throwable
        get() = ioException
}
