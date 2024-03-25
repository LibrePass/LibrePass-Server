package dev.medzik.librepass.client.api

import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.errors.ApiException

/**
 * Check connection with the API server.
 *
 * @param apiUrl server api url
 * @return whether connection has been established and if the server is LibrePass
 */
fun checkApiConnection(apiUrl: String): Boolean {
    val client = Client(apiUrl)
    try {
        val response = client.get("/actuator/info")
        return response.contains("\"group\":\"dev.medzik.librepass\"")
    } catch (e: ApiException) {
        return false
    }
}
