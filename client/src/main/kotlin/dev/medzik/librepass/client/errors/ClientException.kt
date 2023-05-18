package dev.medzik.librepass.client.errors

import java.io.IOException

/**
 * Exception thrown when a client error occurs. When failed to execute a request. This exception is thrown.
 * @param ioException The underlying [IOException] that caused this exception to be thrown.
 */
@Suppress("unused")
class ClientException(
    @Suppress("MemberVisibilityCanBePrivate")
    val ioException: IOException
) : Exception() {
    constructor(message: String) : this(IOException(message))

    override val message: String
        get() = "Client error occurred while executing request ${ioException.message}"
}
