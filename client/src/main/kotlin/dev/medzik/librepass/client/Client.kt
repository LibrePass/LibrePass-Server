package dev.medzik.librepass.client

import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.ResponseError
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class Client(
    accessToken: String? = null,
    private val apiURL: String = DefaultApiUrl
) {
    companion object {
        /**
         * Default API Instance URL
         */
        const val DefaultApiUrl = "https://librepass-api.medzik.dev"

        // JSON media type
        private val MediaTypeJson: MediaType = "application/json; charset=utf-8".toMediaType()

        // HTTP client instance
        private val HttpClient = OkHttpClient()
    }

    // create authorization header if access token is provided
    private val authorizationHeader = if (accessToken.isNullOrEmpty()) "" else "Bearer $accessToken"

    /**
     * Send a GET request to the API
     * @param endpoint endpoint of the API
     * @return response body
     */
    @Throws(ClientException::class, ApiException::class)
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
    @Throws(ClientException::class, ApiException::class)
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
    @Throws(ClientException::class, ApiException::class)
    fun post(endpoint: String, json: String): String {
        val body = json.toRequestBody(MediaTypeJson)

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
    @Throws(ClientException::class, ApiException::class)
    fun patch(endpoint: String, json: String): String {
        val body = json.toRequestBody(MediaTypeJson)

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
    @Throws(ClientException::class, ApiException::class)
    fun put(endpoint: String, json: String): String {
        val body = json.toRequestBody(MediaTypeJson)

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
    @Throws(ClientException::class, ApiException::class)
    private fun executeAndExtractBody(request: Request): String {
        try {
            // send request
            val response = HttpClient.newCall(request).execute()

            // extract from response
            val statusCode = response.code
            val body = response.body.string()

            // error handling
            if (statusCode >= 300) {
                throw ApiException(
                    status = statusCode,
                    error = Json.decodeFromString(ResponseError.serializer(), body).error
                )
            }

            return body
        } catch (e: IOException) {
            throw ClientException(e)
        }
    }
}
