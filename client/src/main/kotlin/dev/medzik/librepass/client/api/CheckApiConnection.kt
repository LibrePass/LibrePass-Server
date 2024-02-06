package dev.medzik.librepass.client.api

import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.errors.ApiException

/**
 * Check if the API at the specified URL is valid.
 *
 * @param apiUrl the URL of the API to check
 * @return `true` if the API is valid, `false` otherwise
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
