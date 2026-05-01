package com.notification.orchestrator.queue;

import com.notification.orchestrator.batching.NotificationBatcher;
import com.notification.orchestrator.config.NotificationProperties;
import com.notification.orchestrator.ratelimiter.UserRateLimiter;
import com.notification.orchestrator.service.NotificationDeliveryService;
import com.notification.orchestrator.model.Notification;
import com.notification.orchestrator.model.UserPreferences;
import com.notification.orchestrator.model.enums.NotificationStatus;
import com.notification.orchestrator.model.enums.Priority;
import com.notification.orchestrator.scheduler.NotificationScheduler;
import com.notification.orchestrator.service.UserPreferenceService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationProcessor {

    private final NotificationProperties properties;
    private final NotificationQueue queue;
    private final NotificationScheduler scheduler;
    private final NotificationDeliveryService deliveryService;
    private final UserPreferenceService userPreferenceService;
    private final NotificationBatcher batcher;
    private final UserRateLimiter rateLimiter;

    private final Set<String> scheduledUsers = ConcurrentHashMap.newKeySet();

    public NotificationProcessor(NotificationProperties properties, NotificationQueue queue,
                                 NotificationScheduler scheduler, NotificationDeliveryService deliveryService,
                                 UserPreferenceService userPreferenceService, NotificationBatcher batcher,
                                 UserRateLimiter rateLimiter) {
        this.properties = properties;
        this.queue = queue;
        this.scheduler = scheduler;
        this.deliveryService = deliveryService;
        this.userPreferenceService = userPreferenceService;
        this.batcher = batcher;
        this.rateLimiter = rateLimiter;
    }

    @PostConstruct
    public void startprocessing() {
        for (int i=0; i<5; i++) {
            startWorker(i);
        }
    }

    private void startWorker(int i) {
        Thread.startVirtualThread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Notification notification = queue.dequeue();
                    System.out.println("Worker " + i + " processing: " + notification.getId());

                    process(notification);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void process(Notification notification) {

        notification.setStatus(NotificationStatus.PROCESSING);

        var prefs = userPreferenceService.getUserPreferences(notification.getUserId());
        boolean isDnd = isWithinDnd(prefs);

        // HIGH priority bypasses DND
        if (isDnd && notification.getPriority() != Priority.HIGH) {

            // Schedule after DND ends
            notification.setScheduledAt(nextAllowedTime(prefs));

        } else {

            // Normal scheduling
            if (notification.getPriority() == Priority.LOW) {
                notification.setScheduledAt(Instant.now().plusSeconds(properties.getScheduling().getLowDelaySeconds()));
            } else if (notification.getPriority() == Priority.MEDIUM) {
                notification.setScheduledAt(Instant.now().plusSeconds(properties.getScheduling().getMediumDelaySeconds()));
            } else {
                notification.setScheduledAt(Instant.now());
            }
        }

        if (notification.getPriority() == Priority.LOW) {

            batcher.addNotification(notification);

            // schedule only once per user
            if (scheduledUsers.add(notification.getUserId())) {

                scheduler.scheduleNotification(notification, () -> {
                    flushBatch(notification.getUserId());
                    scheduledUsers.remove(notification.getUserId());
                });
            }

            return;
        }

        scheduler.scheduleNotification(notification, () -> {
            handleDelivery(notification);
        });
    }

    private void flushBatch(String userId) {

        List<Notification> batch = batcher.getBatch(userId);

        if (batch.isEmpty()) return;

        // Combine messages
        String combinedMessage = "You have " + batch.size() + " new notifications";

        Notification aggregated = Notification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .message(combinedMessage)
                .priority(Priority.LOW)
                .status(NotificationStatus.PROCESSING)
                .createdAt(Instant.now())
                .retryCount(0)
                .maxRetryCount(properties.getRetry().getMaxRetries())
                .build();

        batcher.clearBatch(userId);

        handleDelivery(aggregated);
    }

    private void handleDelivery(Notification notification) {

        if (!rateLimiter.allow(notification.getUserId())) {
            System.out.println("Rate limit exceeded for user: " + notification.getUserId());

            // delay instead of dropping
            notification.setScheduledAt(Instant.now().plusSeconds(60));
            scheduler.scheduleNotification(notification, () -> handleDelivery(notification));
            return;
        }

        try {
            deliveryService.send(notification);
        } catch (Exception e) {
            notification.setRetryCount(notification.getRetryCount() + 1);
            System.out.println("Delivery failed: " + notification.getId() + "retry: " + notification.getRetryCount());

            if (notification.getRetryCount() <= notification.getMaxRetryCount()) {
                // using exponential delay
                long delay = (long) Math.pow(properties.getRetry().getBaseDelaySeconds(),
                        notification.getRetryCount());
                notification.setScheduledAt(Instant.now().plusSeconds(delay));
                System.out.println("Retrying in : " + delay + " ms");
                scheduler.scheduleNotification(notification, () -> handleDelivery(notification));
            } else  {
                notification.setStatus(NotificationStatus.FAILED);
                System.out.println("Permanent Failure : " + notification.getId());
            }
        }
    }

    private boolean isWithinDnd(UserPreferences pref) {

        LocalTime now = LocalTime.now();

        if (pref.getDndStart().isAfter(pref.getDndEnd())) {
            return now.isAfter(pref.getDndStart()) || now.isBefore(pref.getDndEnd());
        }

        return now.isAfter(pref.getDndStart()) && now.isBefore(pref.getDndEnd());
    }

    private Instant nextAllowedTime(UserPreferences pref) {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LocalDateTime nextTime;

        if (pref.getDndStart().isAfter(pref.getDndEnd())) {
            // say its already midnight
            if (now.isBefore(pref.getDndEnd())) {
                // early morning → same day end
                nextTime = LocalDateTime.of(today, pref.getDndEnd());
            } else {
                // late night → next day end
                nextTime = LocalDateTime.of(today.plusDays(1), pref.getDndEnd());
            }

        } else {
            // simple case
            nextTime = LocalDateTime.of(today, pref.getDndEnd());
        }

        return nextTime.atZone(ZoneId.systemDefault()).toInstant();
    }

}
