package com.pulse.event.domain;

import com.pulse.event.entity.IncidentEventType;

import java.time.Instant;
import java.util.UUID;

public record IncidentEventRecorded(
    UUID eventId,
    UUID incidentId,
    IncidentEventType type,
    String message,
    Instant occurredAt
) {
}
