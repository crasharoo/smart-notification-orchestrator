package com.notification.orchestrator.scheduler;

import com.notification.orchestrator.model.Notification;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationScheduler {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);

    public void scheduleNotification(Notification notification, Runnable task) {
        long delay = calculateDelay(notification);
        executorService.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    private long calculateDelay(Notification notification) {
        if (notification.getScheduledAt() == null) {
            return 0;
        }

        long delay = Duration.between(
                Instant.now(),
                notification.getScheduledAt()
        ).toMillis();

        return Math.max(delay, 0);
    }

}
