package com.notification.orchestrator.model;


import com.notification.orchestrator.model.enums.NotificationChannel;
import com.notification.orchestrator.model.enums.NotificationStatus;
import com.notification.orchestrator.model.enums.Priority;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification implements Comparable<Notification> {

    private UUID id;
    private String userId;
    private String message;
    private NotificationChannel channel;   // EMAIL, SMS, PUSH
    private Priority priority;  // HIGH, MEDIUM, LOW
    private NotificationStatus status;    // PENDING, PROCESSING, SENT
    private Instant createdAt;
    private Instant scheduledAt;

    // handle re-try
    private Integer retryCount;
    private Integer maxRetryCount;

    @Override
    public int compareTo(Notification other) {
        // HIGH > MEDIUM > LOW

        int priorityCompare = other.priority.ordinal() - this.priority.ordinal();

        if (priorityCompare != 0) {
            return priorityCompare;
        }

        // Priority is same -> prioritize according to timestamp
        return this.createdAt.compareTo(other.createdAt);
    }


}
