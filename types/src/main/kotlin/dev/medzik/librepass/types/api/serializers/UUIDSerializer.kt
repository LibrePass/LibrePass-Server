package dev.medzik.librepass.types.api.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 * Serializer for [UUID] objects. It serializes [UUID] to string.
 */
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) =
        UUID.fromString(decoder.decodeString())!!

    override fun serialize(encoder: Encoder, value: UUID) =
        encoder.encodeString(value.toString())
}
