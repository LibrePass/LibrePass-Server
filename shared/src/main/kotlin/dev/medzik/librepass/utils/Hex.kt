package dev.medzik.librepass.utils

import org.apache.commons.codec.binary.Hex

fun ByteArray.toHexString(): String = Hex.encodeHexString(this)

fun String.fromHexString(): ByteArray = Hex.decodeHex(this)
