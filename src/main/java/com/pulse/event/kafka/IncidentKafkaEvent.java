package com.pulse.event.kafka;

import com.pulse.event.entity.IncidentEventType;

import java.time.Instant;
import java.util.UUID;

public record IncidentKafkaEvent(
    UUID eventId,
    UUID incidentId,
    IncidentEventType type,
    String message,
    Instant occurredAt
) {
}
