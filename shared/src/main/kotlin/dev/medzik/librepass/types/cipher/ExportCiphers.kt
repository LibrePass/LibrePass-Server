package dev.medzik.librepass.types.cipher

import com.google.gson.Gson

/** Utils for exporting and importing ciphers. */
object ExportCiphers {
    /**
     * Exports the given list of [EncryptedCipher]s as a JSON string.
     *
     * @param ciphers The list of [EncryptedCipher]s to export.
     * @return The JSON string representation of the list of [EncryptedCipher]s.
     */
    @JvmStatic
    fun export(ciphers: List<EncryptedCipher>): String {
        return Gson().toJson(ciphers)
    }

    /**
     * Exports the given list of [Cipher]s as a JSON string, encrypting each [Cipher] using the given AES key.
     *
     * @param ciphers The list of [Cipher]s to export.
     * @param aesKey The AES key to use for encryption.
     * @return The JSON string representation of the list of [EncryptedCipher]s.
     */
    @JvmStatic
    fun export(
        ciphers: List<Cipher>,
        aesKey: ByteArray
    ): String {
        val encryptedCiphers = mutableListOf<EncryptedCipher>()

        for (cipher in ciphers) {
            encryptedCiphers.add(EncryptedCipher(cipher, aesKey))
        }

        return export(encryptedCiphers)
    }

    /**
     * Imports a JSON string representing a list of [EncryptedCipher]s.
     *
     * @param json The JSON string to import.
     * @return The list of [EncryptedCipher]s represented by the JSON string.
     */
    @JvmStatic
    fun import(json: String): List<EncryptedCipher> {
        return Gson().fromJson(json, Array<EncryptedCipher>::class.java).toList()
    }

    /**
     * Imports a JSON string representing a list of [EncryptedCipher]s and decrypting each [EncryptedCipher] to [Cipher] using the given AES key.
     *
     * @param json The JSON string to import.
     * @param aesKey The AES key to use for decryption.
     * @return The list of [Cipher]s represented by the JSON string.
     */
    @JvmStatic
    fun import(
        json: String,
        aesKey: ByteArray
    ): List<Cipher> {
        return import(json).map {
            Cipher(it, aesKey)
        }
    }
}
