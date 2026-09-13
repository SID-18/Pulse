package com.pulse.notification.service;

import com.pulse.notification.dto.NotificationResponse;
import com.pulse.notification.kafka.NotificationEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class NotificationStore {

    private static final int MAX_NOTIFICATIONS = 100;

    private final ConcurrentLinkedDeque<NotificationResponse> notifications =
        new ConcurrentLinkedDeque<>();
    private final Set<UUID> processedEventIds = ConcurrentHashMap.newKeySet();

    public boolean record(NotificationEvent event) {
        if (!processedEventIds.add(event.eventId())) {
            return false;
        }

        notifications.addFirst(new NotificationResponse(
            event.eventId(),
            event.incidentId(),
            event.type(),
            event.message(),
            event.occurredAt(),
            Instant.now()
        ));
        while (notifications.size() > MAX_NOTIFICATIONS) {
            NotificationResponse removed = notifications.pollLast();
            if (removed != null) {
                processedEventIds.remove(removed.eventId());
            }
        }
        return true;
    }

    public List<NotificationResponse> getLatest() {
        return new ArrayList<>(notifications);
    }
}
