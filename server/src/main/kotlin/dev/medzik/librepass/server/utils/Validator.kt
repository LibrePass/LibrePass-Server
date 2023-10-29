package dev.medzik.librepass.server.utils

import org.apache.commons.validator.routines.EmailValidator
import java.util.regex.Pattern

object Validator {
    private val REGEX_PATTERN = Pattern.compile("^\\p{XDigit}+$")

    fun emailValidator(email: String) = EmailValidator.getInstance().isValid(email)

    fun hexValidator(hex: String) = REGEX_PATTERN.matcher(hex).matches()

    fun hexValidator(
        hex: String,
        length: Int
    ) = REGEX_PATTERN.matcher(hex).matches() && hex.length == length * 2
}
