package dev.medzik.librepass.client.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonUtils {
    /** Serialize an object to JSON string. */
    fun serialize(data: Any): String = Gson().toJson(data)

    /** Deserialize JSON string to object. */
    inline fun <reified T> deserialize(data: String): T = Gson().fromJson(data, object : TypeToken<T>() {}.type)
}
