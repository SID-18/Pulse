package com.pulse.incident.dto;

import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.entity.IncidentStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
    UUID id,
    String title,
    String description,
    IncidentSeverity severity,
    IncidentStatus status,
    UUID serviceId,
    Instant createdAt,
    Instant resolvedAt
) implements Serializable {
}
