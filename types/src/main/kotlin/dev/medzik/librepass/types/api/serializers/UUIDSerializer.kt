package dev.medzik.librepass.types.api.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}

//object UUIDListSerializer : KSerializer<List<UUID>> {
//    private val serializer = ListSerializer(UUIDSerializer)
//    override fun serialize(encoder: Encoder, value: List<UUID>) = serializer.serialize(encoder, value)
//
//    override fun deserialize(decoder: Decoder): List<UUID> = serializer.deserialize(decoder)
//
//    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
//}
