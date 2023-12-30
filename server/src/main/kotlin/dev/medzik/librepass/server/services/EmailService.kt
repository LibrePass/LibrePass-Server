package dev.medzik.librepass.server.services

import jakarta.mail.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

/** Service for sending email messages. */
@Service
class EmailService
    @Autowired
    constructor(
        private val emailSender: JavaMailSender,
        @Value("\${smtp.mail.address}")
        private val senderAddress: String,
        @Value("\${server.api.domain}")
        private val apiDomain: String
    ) {
        // get email templates
        private val emailVerificationTemplate =
            this::class.java.getResource("/templates/email-verification.html")?.readText()
                ?: throw Exception("Failed to read `email verification` email template")
        private val changeEmailVerificationTemplate =
            this::class.java.getResource("/templates/change-email-verification.html")?.readText()
                ?: throw Exception("Failed to read `change email verification` email template")
        private val newLoginTemplate =
            this::class.java.getResource("/templates/new-login.html")?.readText()
                ?: throw Exception("Failed to read `new login` email template")
        private val passwordHintTemplate =
            this::class.java.getResource("/templates/password-hint.html")?.readText()
                ?: throw Exception("Failed to read `password hint` email template")

        /**
         * Email the given address.
         *
         * @param to The email address.
         * @param subject The subject of the email.
         * @param body The body of the email.
         */
        fun send(
            to: String,
            subject: String,
            body: String
        ) {
            val message = emailSender.createMimeMessage()
            message.setFrom(senderAddress)
            message.setRecipients(Message.RecipientType.TO, to)
            message.subject = subject
            message.setText(body, "utf-8", "html")

            emailSender.send(message)
        }

        /** Email the given address with the given email verification code. */
        fun sendEmailVerification(
            to: String,
            userId: String,
            code: String
        ) {
            val url = "https://$apiDomain/api/auth/verifyEmail?user=$userId&code=$code"

            val subject = "Activate your LibrePass account"
            val body =
                emailVerificationTemplate
                    .replace("{{url}}", url)

            send(to, subject, body)
        }

        /** Email the given address with the given change email verification code. */
        fun sendChangeEmailVerification(
            oldEmail: String,
            newEmail: String,
            userId: String,
            code: String
        ) {
            val url = "https://$apiDomain/api/user/verifyNewEmail?user=$userId&code=$code"

            val subject = "LibrePass Email Address Change Verification"
            val body =
                changeEmailVerificationTemplate
                    .replace("{{oldEmail}}", oldEmail)
                    .replace("{{newEmail}}", newEmail)
                    .replace("{{url}}", url)

            send(newEmail, subject, body)
        }

        /** Email the given address with the new login. */
        fun sendNewLogin(
            to: String,
            ip: String
        ) {
            val subject = "New Login Detected - LibrePass"
            val body =
                newLoginTemplate
                    .replace("{{ipAddress}}", ip)

            send(to, subject, body)
        }

        /** Email the given address with the password hint. */
        fun sendPasswordHint(
            to: String,
            hint: String?
        ) {
            val subject = "Your LibrePass password hint"
            val body =
                passwordHintTemplate
                    .replace("{{passwordHint}}", hint ?: "[No password hint set]")

            send(to, subject, body)
        }
    }
