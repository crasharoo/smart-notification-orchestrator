package com.notification.orchestrator.ratelimiter;

import com.notification.orchestrator.config.NotificationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserRateLimiter {

    private final NotificationProperties properties;

    public UserRateLimiter(NotificationProperties properties) {
        this.properties = properties;
    }

    private static class Bucket {
        int tokens;
        Instant lastRefillTime;

        Bucket(int tokens, Instant lastRefillTime) {
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }

    private final Map<String, Bucket> bucketMap = new ConcurrentHashMap<>();
     //tokens per minute

    public synchronized boolean allow(String userId) {
        Bucket bucket = bucketMap.computeIfAbsent(
                userId,
                k -> new Bucket(properties.getRateLimit().getCapacity(), Instant.now())
        );

        refillBucket(bucket);

        if (bucket.tokens > 0) {
            bucket.tokens--;
            return true;
        }

        return false;
    }

    private void refillBucket(Bucket bucket) {
        long minutes = Duration.between(bucket.lastRefillTime, Instant.now()).toMinutes();

        // refills allowed only once 1 minute has passed
        if (minutes > 0) {
            int tokensToAdd = (int) minutes * properties.getRateLimit().getRefillPerMinute();

            bucket.tokens = Math.min(properties.getRateLimit().getCapacity(), bucket.tokens + tokensToAdd);
            bucket.lastRefillTime = Instant.now();
        }
    }

}
