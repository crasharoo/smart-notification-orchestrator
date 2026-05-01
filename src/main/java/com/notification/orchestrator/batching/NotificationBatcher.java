package com.notification.orchestrator.batching;

import com.notification.orchestrator.model.Notification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class NotificationBatcher {

    private final Map<String, List<Notification>> batchStore = new ConcurrentHashMap<>();

    public void addNotification(Notification notification) {
        batchStore
                .computeIfAbsent(notification.getUserId(), k -> new ArrayList<>())
                .add(notification);
    }

    public List<Notification> getBatch(String userId) {
        return batchStore.get(userId);
    }

    public void clearBatch(String userId) {
        batchStore.remove(userId);
    }
}
