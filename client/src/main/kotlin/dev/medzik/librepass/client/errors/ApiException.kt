package dev.medzik.librepass.client.errors

@Suppress("unused")
class ApiException(
    val status: Number,
    val error: String
) : Exception() {
    override val message: String
        get() = "API error: $error (code $status)"

    /**
     * Check if the error is a 401 Unauthorized
     */
    fun isUnauthorized(): Boolean {
        return status == 401
    }
}
