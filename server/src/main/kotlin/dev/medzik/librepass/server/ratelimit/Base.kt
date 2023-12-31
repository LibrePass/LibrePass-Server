package dev.medzik.librepass.server.ratelimit

import io.github.bucket4j.Bucket
import java.util.concurrent.ConcurrentHashMap

abstract class BaseRateLimitConfig {
    private var cache: Map<String, Bucket> = ConcurrentHashMap()

    fun resolveBucket(key: String): Bucket {
        return cache[key] ?: newBucket().also { cache += key to it }
    }

    abstract fun newBucket(): Bucket
}
