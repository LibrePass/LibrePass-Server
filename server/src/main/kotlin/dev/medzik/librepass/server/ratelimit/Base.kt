package dev.medzik.librepass.server.ratelimit

import io.github.bucket4j.Bucket
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

abstract class BaseRateLimitConfig {
    private var cache: Map<String, Bucket> = ConcurrentHashMap()

    fun resolveBucket(key: String): Bucket {
        return cache[key] ?: newBucket().also { cache += key to it }
    }

    abstract val capacity: Long
    abstract val refill: Long
    abstract val refillDuration: Duration

    private fun newBucket(): Bucket {
        return Bucket.builder()
            .addLimit { limit ->
                limit.capacity(capacity)
                    .refillGreedy(refill, refillDuration)
            }
            .build()
    }
}
