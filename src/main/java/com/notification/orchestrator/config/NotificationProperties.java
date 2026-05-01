package com.notification.orchestrator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private Retry retry;
    private RateLimit rateLimit;
    private Batching batching;
    private Scheduling scheduling;

    @Data
    public static class Retry {
        private int maxRetries;
        private int baseDelaySeconds;
    }

    @Data
    public static class RateLimit {
        private int capacity;
        private int refillPerMinute;
    }

    @Data
    public static class Batching {
        private int windowSeconds;
    }

    @Data
    public static class Scheduling {
        private int lowDelaySeconds;
        private int mediumDelaySeconds;
    }
}