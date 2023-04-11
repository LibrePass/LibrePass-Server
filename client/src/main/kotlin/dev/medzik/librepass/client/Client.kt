package dev.medzik.librepass.client

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class Client(
    accessToken: String?,
    private val apiURL: String
) {
    companion object {
        const val DefaultApiUrl = "https://librepass-api.medzik.dev"
    }

    private val mediaTypeJson: MediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient()

    private val authorizationHeader = if (accessToken.isNullOrEmpty()) "" else "Bearer $accessToken"

    /**
     * Send a GET request to the API
     * @param endpoint endpoint of the API
     * @return response body
     */
    @Throws(IOException::class)
    fun get(endpoint: String): String {
        val request = Request.Builder()
            .url(apiURL + endpoint)
            .addHeader("Authorization", authorizationHeader)
            .get()
            .build()

        return executeAndExtractBody(request)
    }

    /**
     * Send a DELETE request to the API
     * @param endpoint endpoint of the API
     * @return response body
     */
    @Throws(IOException::class)
    fun delete(endpoint: String): String {
        val request = Request.Builder()
            .url(apiURL + endpoint)
            .addHeader("Authorization", authorizationHeader)
            .delete()
            .build()

        return executeAndExtractBody(request)
    }

    /**
     * Send a POST request to the API
     * @param endpoint endpoint of the API
     * @param json JSON body of the request
     * @return response body
     */
    @Throws(IOException::class)
    fun post(endpoint: String, json: String): String {
        val body = json.toRequestBody(mediaTypeJson)

        val request = Request.Builder()
            .url(apiURL + endpoint)
            .addHeader("Authorization", authorizationHeader)
            .post(body)
            .build()

        return executeAndExtractBody(request)
    }

    /**
     * Send a PATCH request to the API
     * @param endpoint endpoint of the API
     * @param json JSON body of the request
     * @return response body
     */
    @Throws(IOException::class)
    fun patch(endpoint: String, json: String): String {
        val body = json.toRequestBody(mediaTypeJson)

        val request = Request.Builder()
            .url(apiURL + endpoint)
            .addHeader("Authorization", authorizationHeader)
            .patch(body)
            .build()

        return executeAndExtractBody(request)
    }

    /**
     * Send a PUT request to the API
     * @param endpoint endpoint of the API
     * @param json JSON body of the request
     * @return response body
     */
    @Throws(IOException::class)
    fun put(endpoint: String, json: String): String {
        val body = json.toRequestBody(mediaTypeJson)

        val request = Request.Builder()
            .url(apiURL + endpoint)
            .addHeader("Authorization", authorizationHeader)
            .put(body)
            .build()

        return executeAndExtractBody(request)
    }

    /**
     * Execute a request and extract the body
     * @param request request to execute
     * @return response body
     */
    @Throws(IOException::class)
    private fun executeAndExtractBody(request: Request): String {
        // send request
        val response = client.newCall(request).execute()

        // extract from response
        val statusCode = response.code
        val body = response.body.string()

        // error handling
        if (statusCode >= 300) {
            throw IOException("status = $statusCode, body = $body")
        }

        return body
    }
}
