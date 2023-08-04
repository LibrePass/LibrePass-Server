package dev.medzik.librepass.utils

import org.apache.commons.codec.binary.Base32
import org.apache.commons.codec.binary.Hex
import java.security.SecureRandom
import de.taimos.totp.TOTP as TOTPimpl

object TOTP {
    fun generateSecretKey(): String {
        val random = SecureRandom()
        val bytes = ByteArray(20)
        random.nextBytes(bytes)
        val base32 = Base32()
        return base32.encodeToString(bytes)
    }

    fun getTOTPCode(secretKey: String?): String {
        val base32 = Base32()
        val bytes = base32.decode(secretKey)
        val hexKey = Hex.encodeHexString(bytes)
        return TOTPimpl.getOTP(hexKey)
    }
}
