package dev.medzik.librepass.client.utils

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

object JsonUtils {
    /**
     * Serialize data to JSON string.
     * @param data data to serialize
     */
    @OptIn(InternalSerializationApi::class)
    inline fun <reified T : Any> serialize(data: T): String {
        val serializer = T::class.serializer()

        return Json.encodeToString(serializer, data)
    }

    /**
     * Deserialize JSON string to data.
     * @param data JSON string
     */
    @OptIn(InternalSerializationApi::class)
    inline fun <reified T : Any> deserialize(data: String): T {
        val serializer = T::class.serializer()
        return Json.decodeFromString(serializer, data)
    }

    /**
     * Deserialize JSON string to data.
     * @param data JSON string
     */
    @OptIn(InternalSerializationApi::class)
    inline fun <reified T : Any> deserializeList(data: String): List<T> {
        val serializer = T::class.serializer()
        return Json.decodeFromString(ListSerializer(serializer), data)
    }
}
