package dev.medzik.librepass.types.cipher.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * CipherField is a representation of a custom field of a cipher.
 * @property name field name
 * @property type field type
 * @property value value of the field
 * @see CipherFieldType
 */
@Serializable
data class CipherField(
    val name: String,
    val type: CipherFieldType,
    val value: String
)

/**
 * CipherFieldType is an enum class that represents the type of cipher field.
 */
@Serializable(with = CipherFieldTypeSerializer::class)
enum class CipherFieldType {
    Text,
    Hidden
}

/**
 * Serializer for [CipherFieldType] enum class. Serializes to and from [Int].
 */
private object CipherFieldTypeSerializer : KSerializer<CipherFieldType> {
    override val descriptor =
        PrimitiveSerialDescriptor("CipherFieldType", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: CipherFieldType) =
        encoder.encodeInt(value.ordinal)

    override fun deserialize(decoder: Decoder) =
        CipherFieldType.values()[decoder.decodeInt()]
}
