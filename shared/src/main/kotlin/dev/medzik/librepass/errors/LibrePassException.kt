package dev.medzik.librepass.errors

open class LibrePassException(val enum: LibrePassExceptions) : RuntimeException()
