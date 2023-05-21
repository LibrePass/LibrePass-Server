package dev.medzik.librepass.server.utils

import dev.medzik.libcrypto.Argon2HashingFunction

/**
 * Argon2id hashing functions with default values.
 */
val Argon2DefaultHasher = Argon2HashingFunction(256 / 8, 1, 15 * 1024, 1)
