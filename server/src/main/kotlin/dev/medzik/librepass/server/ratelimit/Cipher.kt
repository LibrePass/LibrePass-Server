package dev.medzik.librepass.server.ratelimit

import java.time.Duration

/**
 * Rate limit configuration for CipherController.
 *
 * Maximum of 200 tokens, refilled after one minute by 100 tokens.
 */
class CipherControllerRateLimitConfig : BaseRateLimitConfig() {
    override val capacity: Long = 200
    override val refill: Long = 100
    override val refillDuration: Duration = Duration.ofMinutes(1)
}
