package dev.medzik.librepass.types.api

data class ResponseData<T>(val data: T, val code: String?, val status: Number)
