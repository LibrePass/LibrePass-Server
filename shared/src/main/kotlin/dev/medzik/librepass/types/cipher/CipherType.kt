package dev.medzik.librepass.types.cipher

/** The type of cipher. */
enum class CipherType {
    Login,
    SecureNote,
    Card;

    companion object {
        /** Returns the [CipherType] from the given [type] integer. */
        fun from(type: Int) = entries[type]
    }
}
