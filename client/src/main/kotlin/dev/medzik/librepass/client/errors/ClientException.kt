package dev.medzik.librepass.client.errors

import java.io.IOException

class ClientException(
    val ioException: IOException
) : Exception() {
    override val message: String
        get() = "Client error occurred while executing request ${ioException.message}"
}
