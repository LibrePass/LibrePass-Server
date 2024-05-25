package dev.medzik.librepass.client.api

import com.google.gson.JsonSyntaxException
import dev.medzik.librepass.client.Client
import dev.medzik.librepass.client.errors.ApiException

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
    } catch (e: ApiException) {
        return false
    } catch (e: JsonSyntaxException) {
        return false
    }
}
