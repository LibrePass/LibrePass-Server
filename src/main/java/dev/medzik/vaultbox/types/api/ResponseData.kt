package dev.medzik.vaultbox.types.api

data class ResponseData<T>(val data: T, val code: String?, val status: Number)
