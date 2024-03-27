package dev.medzik.librepass.server.ratelimit

import java.time.Duration

/**
 * Rate limit configuration for AuthController.
 *
 * Maximum of 20 tokens, refilled after one minute by 10 tokens.
 */
class AuthControllerRateLimitConfig : BaseRateLimitConfig() {
    override val capacity: Long = 20
    override val refill: Long = 10
    override val refillDuration: Duration = Duration.ofMinutes(1)
}

/**
 * Rate limit configuration for AuthController email sending. (Currently used only for sending email with user's password hint)
 *
 * Maximum of two tokens, refilled after five minutes by one token.
 */
class AuthControllerEmailRateLimitConfig : BaseRateLimitConfig() {
    override val capacity: Long = 2
    override val refill: Long = 1
    override val refillDuration: Duration = Duration.ofMinutes(5)
}
