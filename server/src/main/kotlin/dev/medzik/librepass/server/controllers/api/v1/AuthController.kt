package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.librepass.server.services.EmailService
import dev.medzik.librepass.server.services.UserService
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseError
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {
    /**
     * Rate limit for login endpoint per IP address.
     */
    class AuthRateLimitConfig {
        private var cache: Map<String, Bucket> = ConcurrentHashMap()

        fun resolveBucket(key: String): Bucket {
            return cache[key] ?: newBucket().also { cache += key to it }
        }

        private fun newBucket(): Bucket {
            return Bucket.builder()
                .addLimit(Bandwidth.classic(20, Refill.intervally(10, Duration.ofMinutes(1))))
                .build()
        }
    }

    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var emailService: EmailService

    private val rateLimit = AuthRateLimitConfig()

    private val logger = LoggerFactory.getLogger(this::class.java)

    val scope = CoroutineScope(Dispatchers.IO)

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): Response {
        val dbUser = userService.register(request)

        // send email verification
        scope.launch {
            try {
                emailService.sendEmailVerification(
                    to = request.email,
                    user = dbUser.id.toString(),
                    code = dbUser.emailVerificationCode.toString()
                )
            } catch (e: Exception) {
                logger.error("Failed to send email verification", e)
            }
        }

        return ResponseHandler.generateResponse(HttpStatus.CREATED)
    }

    @GetMapping("/userArgon2Parameters")
    fun getUserArgon2Parameters(
        httpServletRequest: HttpServletRequest,
        @RequestParam("email") email: String
    ): Response {
        val ip = httpServletRequest.remoteAddr
        if (!rateLimit.resolveBucket(ip).tryConsume(1)) {
            return ResponseError.TooManyRequests
        }

        val argon2Parameters = userService.getArgon2Parameters(email)
            ?: return ResponseError.InvalidCredentials

        return ResponseHandler.generateResponse(argon2Parameters, HttpStatus.OK)
    }

    @PostMapping("/login")
    fun login(
        httpServletRequest: HttpServletRequest,
        @RequestBody request: LoginRequest
    ): Response {
        val ip = httpServletRequest.remoteAddr
        if (!rateLimit.resolveBucket(ip).tryConsume(1)) {
            return ResponseError.TooManyRequests
        }

        val credentials = userService.login(request.email, request.password) ?: return ResponseError.InvalidCredentials

        return ResponseHandler.generateResponse(credentials, HttpStatus.OK)
    }

    @GetMapping("/verifyEmail")
    fun verifyEmail(
        @RequestParam("user") user: String,
        @RequestParam("code") code: String
    ): Response {
        return if (userService.verifyEmail(userId = user, verificationToken = code)) {
            ResponseHandler.generateResponse(HttpStatus.OK)
        } else {
            ResponseError.InvalidCredentials
        }
    }
}
