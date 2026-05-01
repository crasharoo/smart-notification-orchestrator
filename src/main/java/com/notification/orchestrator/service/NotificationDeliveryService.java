package com.notification.orchestrator.service;

import com.notification.orchestrator.model.Notification;
import com.notification.orchestrator.model.enums.NotificationStatus;
import org.springframework.stereotype.Service;

/**
 * This class will handle the actual notification delivery business logic.
 * For ex. pushing notification, sending mail notification etc.
 */
@Service
public class NotificationDeliveryService {

    public void send(Notification notification) {
        System.out.println("Attempting delivery: " + notification.getId());

        System.out.println("DELIVERED: " + notification.getMessage() + " for user id : " + notification.getId());

        notification.setStatus(NotificationStatus.SENT);
    }

}
