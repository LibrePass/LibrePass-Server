package dev.medzik.librepass.client.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonUtils {
    /**
     * Serialize data to JSON string.
     * @param data data to serialize
     */
    fun serialize(data: Any): String =
        Gson().toJson(data)

    /**
     * Deserialize JSON string to data.
     * @param data JSON string
     */
    inline fun <reified T> deserialize(data: String): T =
        Gson().fromJson(data, object : TypeToken<T>() {}.type)
}
