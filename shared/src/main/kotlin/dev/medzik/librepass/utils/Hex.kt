package dev.medzik.librepass.utils

import dev.medzik.libcrypto.Hex

/** Encodes a byte array into a hex string. */
fun ByteArray.toHexString(): String = Hex.encode(this)

/** Decodes a hex string to a byte array. */
fun String.fromHexString(): ByteArray = Hex.decode(this)
