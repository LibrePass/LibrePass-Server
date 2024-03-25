package dev.medzik.librepass.types.adapters

import com.google.gson.*
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit

internal class DateAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {
    override fun serialize(
        src: Date?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return if (src != null) {
            JsonPrimitive(TimeUnit.MILLISECONDS.toSeconds(src.time))
        } else {
            JsonNull.INSTANCE
        }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Date {
        return if (json != null) {
            Date(TimeUnit.SECONDS.toMillis(json.asLong))
        } else {
            Date()
        }
    }
}
