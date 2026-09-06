package com.pulse.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID eventId,
    UUID incidentId,
    String type,
    String message,
    Instant occurredAt,
    Instant receivedAt
) {
}
