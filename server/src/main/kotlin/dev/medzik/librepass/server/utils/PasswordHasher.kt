package dev.medzik.librepass.server.utils

import dev.medzik.libcrypto.Argon2

/**
 * Argon2id hashing functions with default values for the server.
 */
val Argon2DefaultHasher: Argon2 =
    Argon2.Builder()
        .setHashLength(32) // 256 bits / 8 = 32 bytes
        .setMemory(15 * 1024) // 15 MB
        .setIterations(1)
        .setParallelism(1)
        .build()
