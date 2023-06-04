package dev.medzik.librepass.types.api.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 * Serializer for [Date] objects. It serializes [Date] to unix timestamp (seconds since epoch).
 */
object DateSerializer : KSerializer<Date> {
    override val descriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Date) =
        encoder.encodeLong(value.time / 1000)

    override fun deserialize(decoder: Decoder) = Date(decoder.decodeLong() * 1000)
}
