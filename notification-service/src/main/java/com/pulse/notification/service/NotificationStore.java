package com.pulse.notification.service;

import com.pulse.notification.dto.NotificationResponse;
import com.pulse.notification.kafka.NotificationEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class NotificationStore {

    private static final int MAX_NOTIFICATIONS = 100;

    private final ConcurrentLinkedDeque<NotificationResponse> notifications =
        new ConcurrentLinkedDeque<>();

    public void record(NotificationEvent event) {
        notifications.addFirst(new NotificationResponse(
            event.eventId(),
            event.incidentId(),
            event.type(),
            event.message(),
            event.occurredAt(),
            Instant.now()
        ));
        while (notifications.size() > MAX_NOTIFICATIONS) {
            notifications.pollLast();
        }
    }

    public List<NotificationResponse> getLatest() {
        return new ArrayList<>(notifications);
    }
}
