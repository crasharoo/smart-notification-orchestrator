package com.notification.orchestrator.controller;


import com.notification.orchestrator.config.NotificationProperties;
import com.notification.orchestrator.model.Notification;
import com.notification.orchestrator.model.enums.NotificationStatus;
import com.notification.orchestrator.queue.NotificationQueue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationQueue queue;
    private final NotificationProperties properties;

    public NotificationController(NotificationQueue queue, NotificationProperties properties) {
        this.queue = queue;
        this.properties = properties;
    }

    @PostMapping
    public String createNotification(@RequestBody Notification notification) {
        notification.setId(UUID.randomUUID());
        notification.setCreatedAt(Instant.now());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRetryCount(0);
        notification.setMaxRetryCount(properties.getRetry().getMaxRetries());

        queue.enqueue(notification);
        return "Notification queued successfully";

    }


}
