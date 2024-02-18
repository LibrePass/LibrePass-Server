package dev.medzik.librepass.utils

import dev.medzik.libcrypto.Hex

/** Encodes an array of into a hex string. */
fun ByteArray.toHex(): String = Hex.encode(this)

/** Decodes a hex string into a byte array. */
fun String.fromHex(): ByteArray = Hex.decode(this)
