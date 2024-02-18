package dev.medzik.librepass.server.utils

import dev.medzik.libcrypto.X25519
import dev.medzik.librepass.server.controllers.api.ServerPrivateKey
import dev.medzik.librepass.server.database.UserTable
import dev.medzik.librepass.utils.fromHex
import java.util.regex.Pattern

object Validator {
    private val REGEX_PATTERN = Pattern.compile("^\\p{XDigit}+$")

    fun hexValidator(hex: String) = REGEX_PATTERN.matcher(hex).matches()

    fun validateSharedKey(
        user: UserTable,
        sharedKey: String
    ) = validateSharedKey(
        publicKey = user.publicKey,
        sharedKey
    )

    fun validateSharedKey(
        publicKey: String,
        sharedKey: String
    ): Boolean {
        val oldSharedKey = X25519.computeSharedSecret(ServerPrivateKey, publicKey.fromHex())
        return sharedKey.fromHex().contentEquals(oldSharedKey)
    }
}
