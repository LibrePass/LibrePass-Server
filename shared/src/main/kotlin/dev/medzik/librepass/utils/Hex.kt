package dev.medzik.librepass.utils

import dev.medzik.libcrypto.Hex

fun ByteArray.toHexString(): String = Hex.encode(this)

fun String.fromHexString(): ByteArray = Hex.decode(this)
