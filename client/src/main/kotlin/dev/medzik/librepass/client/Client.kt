package dev.medzik.librepass.client

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.medzik.librepass.types.api.ResponseData
import dev.medzik.librepass.types.api.auth.UserCredentials
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class Client(
    private val accessToken: String?,
    private val apiURL: String
) {
    companion object {
        @JvmStatic
        val DefaultApiUrl = "https://librepass-api.medzik.dev"
    }

    private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient()

    private val authorizationHeader = if (accessToken.isNullOrEmpty()) "" else "Bearer $accessToken"

    @Throws(IOException::class)
    fun get(endpoint: String): String {
        val request = Request.Builder()
            .url(apiURL + endpoint)
            .addHeader("Authorization", authorizationHeader)
            .get()
            .build()

        return executeAndExtractBody(request)
    }

    @Throws(IOException::class)
    fun delete(endpoint: String): String {
        val request = Request.Builder()
            .url(apiURL + endpoint)
            .addHeader("Authorization", authorizationHeader)
            .delete()
            .build()

        return executeAndExtractBody(request)
    }

    @Throws(IOException::class)
    fun post(endpoint: String, json: String): String {
        val body = json.toRequestBody(JSON)

        val request = Request.Builder()
            .url(apiURL + endpoint)
            .addHeader("Authorization", authorizationHeader)
            .post(body)
            .build()

        return executeAndExtractBody(request)
    }

    @Throws(IOException::class)
    fun patch(endpoint: String, json: String): String {
        val body = json.toRequestBody(JSON)

        val request = Request.Builder()
            .url(apiURL + endpoint)
            .addHeader("Authorization", authorizationHeader)
            .patch(body)
            .build()

        return executeAndExtractBody(request)
    }

    @Throws(IOException::class)
    fun put(endpoint: String, json: String): String {
        val body = json.toRequestBody(JSON)

        val request = Request.Builder()
            .url(apiURL + endpoint)
            .addHeader("Authorization", authorizationHeader)
            .put(body)
            .build()

        return executeAndExtractBody(request)
    }

    @Throws(IOException::class)
    private fun executeAndExtractBody(request: Request): String {
        // send request
        val response = client.newCall(request).execute()
        val statusCode = response.code
        val body = response.body.string()

        if (statusCode >= 300) {
            throw IOException("status = $statusCode, body = $body")
        }

        return body
    }
}
