package dev.medzik.librepass.utils

import dev.medzik.libcrypto.Aes

/** Encrypted [String]. */
typealias EncryptedString = String

/** Decrypts the [EncryptedString]. */
fun EncryptedString.decrypt(key: ByteArray): String {
    return String(Aes.decrypt(Aes.GCM, key, this))
}

/** Encrypts the [EncryptedString]. */
fun String.encrypt(key: ByteArray): EncryptedString {
    return Aes.encrypt(Aes.GCM, key, this.toByteArray())
}
