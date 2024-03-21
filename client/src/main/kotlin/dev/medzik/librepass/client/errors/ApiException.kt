package dev.medzik.librepass.client.errors

import dev.medzik.librepass.errors.ServerError
import dev.medzik.librepass.types.api.ResponseError

/**
 * Thrown when the server returns an error.
 *
 * @param status The HTTP status code returned by the server.
 * @param response The response returned by the server.
 */
class ApiException(
    val status: Number,
    val response: ResponseError
) : Exception() {
    override val message: String = "HTTP $status: $response"

    /**
     * Returns the error as [ServerError].
     */
    fun getServerError(): ServerError? {
        for (x in ServerError.entries) {
            if (x.error == response.error) {
                return x
            }
        }

        return null
    }
}
