package com.pulse.notification.kafka;

import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
    UUID eventId,
    UUID incidentId,
    UUID recipientUserId,
    UUID recipientTeamId,
    String type,
    String message,
    Instant occurredAt
) {
}
