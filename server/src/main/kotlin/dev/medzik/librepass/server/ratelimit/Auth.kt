package dev.medzik.librepass.server.ratelimit

import io.github.bucket4j.BandwidthBuilder
import io.github.bucket4j.Bucket
import java.time.Duration

/**
 * Rate limit configuration for AuthController.
 *
 * Maximum of 20 tokens, refilled after one minute by 10 tokens.
 */
class AuthControllerRateLimitConfig : BaseRateLimitConfig() {
    override fun newBucket(): Bucket {
        return Bucket.builder()
            .addLimit {
                    limit: BandwidthBuilder.BandwidthBuilderCapacityStage ->
                limit.capacity(20)
                    .refillGreedy(10, Duration.ofMinutes(1))
            }
            .build()
    }
}

/**
 * Rate limit configuration for AuthController email sending. (Currently used for sending email with user's password hint)
 *
 * Maximum of two tokens, refilled after five minutes by one token.
 */
class AuthControllerEmailRateLimitConfig : BaseRateLimitConfig() {
    override fun newBucket(): Bucket {
        return Bucket.builder()
            .addLimit {
                    limit: BandwidthBuilder.BandwidthBuilderCapacityStage ->
                limit.capacity(2)
                    .refillGreedy(1, Duration.ofMinutes(5))
            }
            .build()
    }
}
