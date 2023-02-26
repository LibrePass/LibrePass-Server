package dev.medzik.librepass.types.api

import com.google.gson.Gson
import dev.medzik.libcrypto.AesCbc
import java.util.*

class CipherData {
    lateinit var name: String
    var username: String? = null
    var password: String? = null
    var uris: Array<String>? = null
    var twoFactor: String? = null
    var notes: String? = null

    var customFields: Array<String>? = null

    fun encrypt(key: String): String {
        val data = Gson().toJson(this)
        return AesCbc.encrypt(data, key)
    }
}

class Cipher {
    lateinit var id: UUID

    lateinit var owner: UUID

    lateinit var type: Number
    lateinit var data: CipherData

    var favorite: Boolean = false
    var directory: UUID? = null
    var rePrompt: Boolean = false

    var created: Date? = null
    var lastModified: Date? = null

    fun from(cipher: EncryptedCipher, key: String) {
        this.id = cipher.id
        this.owner = cipher.owner

        this.type = cipher.type
        this.data = cipher.decrypt(key)
        this.favorite = cipher.favorite
        this.directory = cipher.directory
        this.rePrompt = cipher.rePrompt

        this.created = cipher.created
        this.lastModified = cipher.lastModified
    }

    fun toEncryptedCipher(key: String): EncryptedCipher {
        val encryptedCipher = EncryptedCipher()
        encryptedCipher.from(this, key)
        return encryptedCipher
    }
}

class EncryptedCipher {
    lateinit var id: UUID

    lateinit var owner: UUID

    lateinit var type: Number
    lateinit var data: String

    var favorite: Boolean = false
    var directory: UUID? = null
    var rePrompt: Boolean = false

    var created: Date? = null
    var lastModified: Date? = null

    fun decrypt(key: String): CipherData {
        val data = AesCbc.decrypt(data, key)
        return Gson().fromJson(data, CipherData::class.java)
    }

    fun from(cipher: Cipher, key: String) {
        this.id = cipher.id
        this.owner = cipher.owner

        this.type = cipher.type
        this.data = cipher.data.encrypt(key)
        this.favorite = cipher.favorite
        this.directory = cipher.directory
        this.rePrompt = cipher.rePrompt

        this.created = cipher.created
        this.lastModified = cipher.lastModified
    }

    fun toCipher(key: String): Cipher {
        val cipher = Cipher()
        cipher.from(this, key)
        return cipher
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }
}
