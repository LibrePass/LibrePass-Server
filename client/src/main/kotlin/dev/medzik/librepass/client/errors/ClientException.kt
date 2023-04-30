package dev.medzik.librepass.client.errors

import java.io.IOException

@Suppress("unused")
class ClientException(
    @Suppress("MemberVisibilityCanBePrivate")
    val ioException: IOException
) : Exception() {
    override val message: String
        get() = "Client error occurred while executing request ${ioException.message}"
}
