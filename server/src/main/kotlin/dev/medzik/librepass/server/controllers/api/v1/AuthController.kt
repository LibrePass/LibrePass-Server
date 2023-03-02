package dev.medzik.librepass.server.controllers.api.v1

import dev.medzik.librepass.server.services.EmailService
import dev.medzik.librepass.server.services.UserService
import dev.medzik.librepass.server.utils.Response
import dev.medzik.librepass.server.utils.ResponseError
import dev.medzik.librepass.server.utils.ResponseHandler
import dev.medzik.librepass.types.api.auth.LoginRequest
import dev.medzik.librepass.types.api.auth.RefreshRequest
import dev.medzik.librepass.types.api.auth.RegisterRequest
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

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

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {
    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var emailService: EmailService

    private val rateLimit = AuthRateLimitConfig()

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): Response {
        val verificationToken = userService.register(request.email, request.password, request.passwordHint, request.encryptionKey)

        // TODO: run in background
        emailService.sendEmailVerification(request.email, verificationToken)

        return ResponseHandler.generateResponse(HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(httpServletRequest: HttpServletRequest, @RequestBody request: LoginRequest): Response {
        val credentials = userService.login(request.email, request.password) ?: return ResponseError.InvalidCredentials

        val ip = httpServletRequest.remoteAddr
        if (!rateLimit.resolveBucket(ip).tryConsume(1)) {
            return ResponseError.TooManyRequests
        }

        return ResponseHandler.generateResponse(credentials, HttpStatus.OK)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): Response {
        val credentials = userService.refreshToken(request.refreshToken) ?: return ResponseError.InvalidCredentials
        return ResponseHandler.generateResponse(credentials, HttpStatus.OK)
    }

    @GetMapping("/verifyEmail")
    fun verifyEmail(): Response {
        return ResponseHandler.generateResponse(HttpStatus.NOT_IMPLEMENTED)
    }
}
