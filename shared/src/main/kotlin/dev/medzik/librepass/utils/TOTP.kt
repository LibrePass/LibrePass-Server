package dev.medzik.librepass.utils

import com.bastiaanjansen.otp.HMACAlgorithm
import com.bastiaanjansen.otp.HOTPGenerator
import com.bastiaanjansen.otp.SecretGenerator
import com.bastiaanjansen.otp.TOTPGenerator
import org.apache.commons.codec.binary.Base32
import java.time.Duration

object TOTP {
    /** Generates a random two-factor secret key. */
    fun generateSecretKey(): String {
        val bytes = SecretGenerator.generate()
        val base32 = Base32()
        return base32.encodeToString(bytes)
    }

    /**
     * Generates a new two-factor code.
     *
     * @param secret The secret key of the code to generate.
     * @return The generated two-factor code.
     */
    fun getTOTPCode(secret: String): String {
        val totpGenerator = initializeTOTPGenerator(secret)
        return totpGenerator.now()
    }

    /**
     * Validates the two-factor code with the given secret key.
     *
     * @param secret The secret key of the code to validate.
     * @param otpCode The code to validate.
     * @return True if the code is valid, false otherwise.
     */
    fun validate(
        secret: String,
        otpCode: String
    ): Boolean {
        val totpGenerator = initializeTOTPGenerator(secret)
        // Check the current code and the previous two and the next two.
        return totpGenerator.verify(otpCode, 2)
    }

    private fun initializeTOTPGenerator(secret: String): TOTPGenerator {
        val base32 = Base32()
        val bytes = base32.decode(secret)

        val totp =
            TOTPGenerator.Builder(bytes)
                .withHOTPGenerator { builder: HOTPGenerator.Builder ->
                    builder.withPasswordLength(6)
                    builder.withAlgorithm(HMACAlgorithm.SHA1) // SHA256 and SHA512 are also supported
                }
                .withPeriod(Duration.ofSeconds(30))

        return totp.build()
    }
}
