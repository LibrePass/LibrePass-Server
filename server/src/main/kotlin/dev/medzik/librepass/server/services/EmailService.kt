package dev.medzik.librepass.server.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
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

    /**
     * Email the given address.
     * @param to The email address to send the email to.
     * @param subject The subject of the email.
     * @param body The body of the email.
     */
    fun send(to: String, subject: String, body: String) {
        val message = SimpleMailMessage()
        message.from = "LibrePass <$senderAddress>"
        message.setTo(to)
        message.subject = subject
        message.text = body

        emailSender.send(message)
    }

    /**
     * Email the given address with the given code.
     * @param to The email address to send the email to.
     * @param code The code to send.
     */
    suspend fun sendEmailVerification(to: String, code: String) {
        // TODO: Use a template engine to generate the email body.
        val subject = "Activate your LibrePass account"
        val body = "Click here to activate your account https://$apiDomain/api/v1/auth/verifyEmail?code=$code"
        send(to, subject, body)
    }
}
