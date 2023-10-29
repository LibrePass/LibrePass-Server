package dev.medzik.librepass.server.utils

import java.util.*

fun checkIfElapsed(
    start: Date,
    end: Date,
    minutes: Int
): Boolean {
    val elapsedMilliseconds = end.time - start.time
    val elapsedMinutes = elapsedMilliseconds / (60 * 1000)
    return elapsedMinutes >= minutes
}
