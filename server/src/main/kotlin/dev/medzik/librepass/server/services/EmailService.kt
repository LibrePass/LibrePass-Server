package dev.medzik.librepass.server.services

import jakarta.mail.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

/**
 * Service for sending emails.
 */
@Service
class EmailService {
    @Autowired
    private lateinit var emailSender: JavaMailSender
    @Value("\${smtp.mail.address}")
    private lateinit var senderAddress: String

    @Value("\${librepass.api.domain}")
    private lateinit var apiDomain: String

    // get email template
    private val emailVerificationTemplate = this::class.java.getResource("/templates/email-verification.html")?.readText()
        ?: throw Exception("Could not read `email verification` email template")
    private val passwordHintTemplate = this::class.java.getResource("/templates/password-hint.html")?.readText()
        ?: throw Exception("Could not read `password hint` email template")

    /**
     * Email the given address.
     * @param to email address to send to
     * @param subject subject of the email
     * @param body body of the email
     */
    fun send(to: String, subject: String, body: String) {
        val message = emailSender.createMimeMessage()
        message.setFrom(senderAddress)
        message.setRecipients(Message.RecipientType.TO, to)
        message.subject = subject
        message.setText(body, "utf-8", "html")

        emailSender.send(message)
    }

    /**
     * Email the given address with the given code.
     */
    fun sendEmailVerification(to: String, user: String, code: String) {
        val url = "https://$apiDomain/api/v1/auth/verifyEmail?user=$user&code=$code"

        val subject = "Activate your LibrePass account"
        val body = emailVerificationTemplate
            .replace("{{url}}", url)

        send(to, subject, body)
    }

    /**
     * Email the given address with the password hint.
     */
    fun sendPasswordHint(to: String, hint: String?) {
        val subject = "Your LibrePass password hint"
        val body = passwordHintTemplate
            .replace("{{passwordHint}}", hint ?: "[No password hint set]")

        send(to, subject, body)
    }
}
