package com.pulse.notification.service;

import com.pulse.notification.dto.NotificationResponse;
import com.pulse.notification.kafka.NotificationEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
    private final ConcurrentHashMap<UUID, Set<UUID>> readByUsers = new ConcurrentHashMap<>();

    public boolean record(NotificationEvent event) {
        if (!processedEventIds.add(event.eventId())) {
            return false;
        }

        notifications.addFirst(new NotificationResponse(
            event.eventId(),
            event.incidentId(),
            event.recipientUserId(),
            event.recipientTeamId(),
            event.type(),
            event.message(),
            event.occurredAt(),
            Instant.now(),
            false
        ));
        while (notifications.size() > MAX_NOTIFICATIONS) {
            NotificationResponse removed = notifications.pollLast();
            if (removed != null) {
                processedEventIds.remove(removed.eventId());
                readByUsers.remove(removed.eventId());
            }
        }
        return true;
    }

    public List<NotificationResponse> getLatest(UUID recipientUserId, UUID recipientTeamId) {
        return notifications.stream()
            .filter(notification -> (recipientUserId == null && recipientTeamId == null)
                || recipientUserId != null && recipientUserId.equals(notification.recipientUserId())
                || recipientTeamId != null && recipientTeamId.equals(notification.recipientTeamId()))
            .map(notification -> toResponse(notification, recipientUserId))
            .toList();
    }

    public boolean markAsRead(UUID eventId, UUID recipientUserId, UUID recipientTeamId) {
        boolean visibleToRecipient = notifications.stream().anyMatch(notification ->
            notification.eventId().equals(eventId)
                && (recipientUserId.equals(notification.recipientUserId())
                    || recipientTeamId != null && recipientTeamId.equals(notification.recipientTeamId()))
        );
        if (!visibleToRecipient) {
            return false;
        }
        readByUsers.computeIfAbsent(eventId, ignored -> ConcurrentHashMap.newKeySet())
            .add(recipientUserId);
        return true;
    }

    private NotificationResponse toResponse(NotificationResponse notification, UUID recipientUserId) {
        boolean read = recipientUserId != null
            && readByUsers.getOrDefault(notification.eventId(), Set.of()).contains(recipientUserId);
        return new NotificationResponse(
            notification.eventId(), notification.incidentId(), notification.recipientUserId(),
            notification.recipientTeamId(), notification.type(), notification.message(),
            notification.occurredAt(), notification.receivedAt(), read
        );
    }
}
