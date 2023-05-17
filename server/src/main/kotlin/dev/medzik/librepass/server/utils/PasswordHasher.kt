package dev.medzik.librepass.server.utils

import dev.medzik.libcrypto.Argon2HashingFunction

val Argon2DefaultHasher = Argon2HashingFunction(32, 1, 15 * 1024, 1)
