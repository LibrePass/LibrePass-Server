package dev.medzik.librepass.server.services

import jakarta.mail.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService {
    @Autowired
    private lateinit var emailSender: JavaMailSender
    @Value("\${smtp.mail.address}")
    private lateinit var senderAddress: String

    @Value("\${librepass.api.domain}")
    private lateinit var apiDomain: String

    // get email template
    private val emailTemplate = this::class.java.getResource("/templates/email.html")?.readText()
        ?: throw Exception("Could not read email template")

    /**
     * Email the given address.
     * @param to The email address to send the email to.
     * @param subject The subject of the email.
     * @param body The body of the email.
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
     * @param to The email address to send the email to.
     * @param code The code to send.
     */
    fun sendEmailVerification(to: String, user: String, code: String) {
        val url = "https://$apiDomain/api/v1/auth/verifyEmail?user=$user&code=$code"

        val subject = "Activate your LibrePass account"
        val body = emailTemplate
            .replace("{{url}}", url)

        send(to, subject, body)
    }
}
