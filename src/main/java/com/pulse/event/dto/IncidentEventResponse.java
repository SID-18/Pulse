package com.pulse.event.dto;

import com.pulse.event.entity.IncidentEventType;

import java.time.Instant;
import java.util.UUID;

public record IncidentEventResponse(
    UUID id,
    UUID incidentId,
    IncidentEventType type,
    String message,
    Instant createdAt
) {
}
