package dev.medzik.librepass.client.api

import dev.medzik.librepass.client.Client

/**
 * Check connection with the API server.
 *
 * @param apiUrl server api url
 * @return Whether connection has been established and if the server is LibrePass.
 */
fun checkApiConnection(apiUrl: String): Boolean {
    val client = Client(apiUrl)
    try {
        val response = client.get("/actuator/info")
        return response.contains("\"group\":\"dev.medzik.librepass\"")
    } catch (e: Exception) {
        return false
    }
}
