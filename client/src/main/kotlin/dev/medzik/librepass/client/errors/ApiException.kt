package dev.medzik.librepass.client.errors

import dev.medzik.librepass.errors.ServerError

/**
 * API Error exception.
 *
 * @property status The HTTP Status code returned by the API.
 * @property error The error returned by the API.
 */
class ApiException(
    val status: Number,
    val error: String
) : Exception() {
    override val message: String = "HTTP $status: $error"

    /** Returns the [ServerError]. */
    fun getServerError() = ServerError.get(error)
}
