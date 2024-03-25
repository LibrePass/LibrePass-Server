package dev.medzik.librepass.client

import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.client.utils.JsonUtils
import dev.medzik.librepass.types.api.ResponseError
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Version of the LibrePass API Client library. */
const val CLIENT_VERSION = "1.5.0"

/** Supported version of the API. */
const val API_VERSION = "1"

/** LibrePass API Servers */
object Server {
    /** Production server instance. */
    const val PRODUCTION = "https://api.librepass.org"

    /** Test server instance. The database from this instance can be deleted at any time! */
    @Suppress("UNUSED")
    const val TEST = "https://api.test.librepass.medzik.dev"
}

/**
 * HTTP Client for sending requests to the API.
 *
 * @param apiURL server api url
 * @param accessToken access token to use for authorization
 */
internal class Client(
    private val apiURL: String,
    private val accessToken: String? = null
) {
    private val httpClient =
        OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

    /**
     * Sends a GET request to the API.
     */
    @Throws(ClientException::class, ApiException::class)
    fun get(endpoint: String): String {
        val request =
            Request.Builder()
                .addLibrePassParameters()
                .url(apiURL + endpoint)
                .get()
                .build()

        return executeAndExtractBody(request)
    }

    /**
     * Sends a DELETE request to the API.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(endpoint: String): String {
        val request =
            Request.Builder()
                .addLibrePassParameters()
                .url(apiURL + endpoint)
                .delete()
                .build()

        return executeAndExtractBody(request)
    }

    /**
     * Sends a DELETE request to the API.
     */
    @Throws(ClientException::class, ApiException::class)
    fun delete(
        endpoint: String,
        json: String
    ): String {
        val request =
            Request.Builder()
                .addLibrePassParameters()
                .url(apiURL + endpoint)
                .delete(makeRequestBodyFromJson(json))
                .build()

        return executeAndExtractBody(request)
    }

    /**
     * Sends a POST request to the API.
     */
    @Throws(ClientException::class, ApiException::class)
    fun post(
        endpoint: String,
        json: String
    ): String {
        val request =
            Request.Builder()
                .addLibrePassParameters()
                .url(apiURL + endpoint)
                .post(makeRequestBodyFromJson(json))
                .build()

        return executeAndExtractBody(request)
    }

    /**
     * Sends a PATCH request to the API.
     */
    @Throws(ClientException::class, ApiException::class)
    fun patch(
        endpoint: String,
        json: String
    ): String {
        val request =
            Request.Builder()
                .addLibrePassParameters()
                .url(apiURL + endpoint)
                .patch(makeRequestBodyFromJson(json))
                .build()

        return executeAndExtractBody(request)
    }

    /**
     * Sends a PUT request to the API.
     */
    @Throws(ClientException::class, ApiException::class)
    fun put(
        endpoint: String,
        json: String
    ): String {
        val request =
            Request.Builder()
                .addLibrePassParameters()
                .url(apiURL + endpoint)
                .put(makeRequestBodyFromJson(json))
                .build()

        return executeAndExtractBody(request)
    }

    private fun Request.Builder.addLibrePassParameters(): Request.Builder {
        if (!accessToken.isNullOrEmpty()) {
            addHeader("Authorization", "Bearer $accessToken")
        }

        addHeader("X-Client", "Java/$CLIENT_VERSION")
        addHeader("X-API", API_VERSION)

        return this
    }

    private fun makeRequestBodyFromJson(json: String): RequestBody {
        return json.toRequestBody("application/json; charset=utf-8".toMediaType())
    }

    /** Execute a request and extract the body from the response. */
    @Throws(ClientException::class, ApiException::class)
    private fun executeAndExtractBody(request: Request): String {
        try {
            // send request
            val response = httpClient.newCall(request).execute()

            // extract from response
            val statusCode = response.code
            val body = response.body.string()

            // error handling
            if (statusCode >= 400) {
                throw ApiException(
                    status = statusCode,
                    response = JsonUtils.deserialize<ResponseError>(body)
                )
            }

            return body
        } catch (e: IOException) {
            throw ClientException(e)
        }
    }
}
