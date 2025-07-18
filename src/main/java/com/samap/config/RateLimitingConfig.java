package com.samap.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j
 */
@Configuration
@Slf4j
public class RateLimitingConfig {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConcurrentHashMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    public RateLimitingConfig(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get rate limiting bucket for login attempts
     */
    public Bucket getLoginBucket(String key) {
        return bucketCache.computeIfAbsent(key, k -> createLoginBucket());
    }

    /**
     * Get rate limiting bucket for API requests
     */
    public Bucket getApiBucket(String key) {
        return bucketCache.computeIfAbsent(key, k -> createApiBucket());
    }

    /**
     * Get rate limiting bucket for admin operations
     */
    public Bucket getAdminBucket(String key) {
        return bucketCache.computeIfAbsent(key, k -> createAdminBucket());
    }

    /**
     * Create bucket for login attempts - 5 attempts per minute
     */
    private Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create bucket for API requests - 100 requests per minute
     */
    private Bucket createApiBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create bucket for admin operations - 50 requests per minute
     */
    private Bucket createAdminBucket() {
        Bandwidth limit = Bandwidth.classic(50, Refill.intervally(50, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Check if request is allowed for login
     */
    public boolean isLoginAllowed(String clientId) {
        return getLoginBucket("login:" + clientId).tryConsume(1);
    }

    /**
     * Check if request is allowed for API
     */
    public boolean isApiAllowed(String clientId) {
        return getApiBucket("api:" + clientId).tryConsume(1);
    }

    /**
     * Check if request is allowed for admin operations
     */
    public boolean isAdminAllowed(String clientId) {
        return getAdminBucket("admin:" + clientId).tryConsume(1);
    }

    /**
     * Get remaining tokens for a bucket
     */
    public long getRemainingTokens(String key, String type) {
        Bucket bucket = switch (type) {
            case "login" -> getLoginBucket("login:" + key);
            case "api" -> getApiBucket("api:" + key);
            case "admin" -> getAdminBucket("admin:" + key);
            default -> getApiBucket("api:" + key);
        };
        return bucket.getAvailableTokens();
    }

    /**
     * Clear rate limiting cache for a key
     */
    public void clearRateLimit(String key) {
        bucketCache.remove(key);
        log.info("Rate limit cleared for key: {}", key);
    }

    /**
     * Get rate limit info
     */
    public RateLimitInfo getRateLimitInfo(String key, String type) {
        Bucket bucket = switch (type) {
            case "login" -> getLoginBucket("login:" + key);
            case "api" -> getApiBucket("api:" + key);
            case "admin" -> getAdminBucket("admin:" + key);
            default -> getApiBucket("api:" + key);
        };

        return RateLimitInfo.builder()
                .remainingTokens(bucket.getAvailableTokens())
                .capacity(bucket.getAvailableTokens())
                .refillRate(getRefillRate(type))
                .build();
    }

    private int getRefillRate(String type) {
        return switch (type) {
            case "login" -> 5;
            case "api" -> 100;
            case "admin" -> 50;
            default -> 100;
        };
    }

    /**
     * Rate limit information
     */
    public static class RateLimitInfo {
        private final long remainingTokens;
        private final long capacity;
        private final int refillRate;

        private RateLimitInfo(long remainingTokens, long capacity, int refillRate) {
            this.remainingTokens = remainingTokens;
            this.capacity = capacity;
            this.refillRate = refillRate;
        }

        public static Builder builder() {
            return new Builder();
        }

        public long getRemainingTokens() { return remainingTokens; }
        public long getCapacity() { return capacity; }
        public int getRefillRate() { return refillRate; }

        public static class Builder {
            private long remainingTokens;
            private long capacity;
            private int refillRate;

            public Builder remainingTokens(long remainingTokens) {
                this.remainingTokens = remainingTokens;
                return this;
            }

            public Builder capacity(long capacity) {
                this.capacity = capacity;
                return this;
            }

            public Builder refillRate(int refillRate) {
                this.refillRate = refillRate;
                return this;
            }

            public RateLimitInfo build() {
                return new RateLimitInfo(remainingTokens, capacity, refillRate);
            }
        }
    }
}
