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
    @Value("\${librepass.api.domain}")
    private lateinit var apiDomain: String

    fun send(to: String, subject: String, body: String) {
        val message = SimpleMailMessage()
//        message.from = senderAddress
        message.setTo(to)
        message.subject = subject
        message.text = body

        emailSender.send(message)
    }

    fun sendEmailVerification(to: String, code: String) {
        val subject = "Activate your LibrePass account"
        val body = "Click here to activate your account https://$apiDomain/api/v1/auth/verifyEmail?code=$code"
        send(to, subject, body)
    }
}
