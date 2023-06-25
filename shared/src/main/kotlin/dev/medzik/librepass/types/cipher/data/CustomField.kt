package dev.medzik.librepass.types.cipher.data

/**
 * CipherField represents a custom field of a cipher.
 * @property name field name
 * @property type field type
 * @property value value of the field
 * @see CipherFieldType
 */
data class CipherField(
    val name: String,
    val type: CipherFieldType,
    val value: String
)

/**
 * CipherFieldType is an enum class that represents the type of cipher field.
 */
enum class CipherFieldType {
    Text,
    Hidden
}

// private object CipherFieldTypeSerializer : KSerializer<CipherFieldType> {
//    override val descriptor =
//        PrimitiveSerialDescriptor("CipherFieldType", PrimitiveKind.INT)
//
//    override fun serialize(encoder: Encoder, value: CipherFieldType) =
//        encoder.encodeInt(value.ordinal)
//
//    override fun deserialize(decoder: Decoder) =
//        CipherFieldType.values()[decoder.decodeInt()]
// }
