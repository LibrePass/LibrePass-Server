package dev.medzik.librepass.server.ratelimit

import io.github.bucket4j.BandwidthBuilder
import io.github.bucket4j.Bucket
import java.time.Duration

/**
 * Rate limit configuration for CipherController.
 *
 * Maximum of 200 tokens, refilled after one minute by 100 tokens.
 */
class CipherControllerRateLimitConfig : BaseRateLimitConfig() {
    override fun newBucket(): Bucket {
        return Bucket.builder()
            .addLimit {
                    limit: BandwidthBuilder.BandwidthBuilderCapacityStage ->
                limit.capacity(200)
                    .refillGreedy(100, Duration.ofMinutes(1))
            }
            .build()
    }
}
