package dev.medzik.librepass.errors

open class BaseException(val error: ServerError) : RuntimeException()
