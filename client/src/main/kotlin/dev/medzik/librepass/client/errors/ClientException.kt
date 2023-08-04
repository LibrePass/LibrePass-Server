package dev.medzik.librepass.client.errors

import java.io.IOException

/**
 * Exception thrown when a client error occurred while executing a request. This is not thrown when the server returns
 * an error response.
 */
class ClientException(ioException: IOException) : Exception() {
    override val message: String = "Client error occurred while executing request ${ioException.message}"
    override val cause: Throwable = ioException
}
