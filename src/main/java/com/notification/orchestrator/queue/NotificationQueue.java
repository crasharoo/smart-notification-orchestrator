package com.notification.orchestrator.queue;

import com.notification.orchestrator.model.Notification;
import org.springframework.stereotype.Component;

import java.util.concurrent.PriorityBlockingQueue;

@Component
public class NotificationQueue {

    private final PriorityBlockingQueue<Notification> queue = new PriorityBlockingQueue<>();

    public void enqueue(Notification notification) {
        queue.offer(notification);
    }

    public Notification dequeue() throws InterruptedException {
        return queue.take();
    }

}
