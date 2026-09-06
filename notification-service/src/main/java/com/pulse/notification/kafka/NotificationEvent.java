package com.pulse.notification.kafka;

import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
    UUID eventId,
    UUID incidentId,
    String type,
    String message,
    Instant occurredAt
) {
}
