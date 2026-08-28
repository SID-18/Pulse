package com.pulse.alert.dto;

import com.pulse.alert.entity.AlertSeverity;
import com.pulse.alert.entity.AlertStatus;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
    UUID id,
    String message,
    AlertSeverity severity,
    AlertStatus status,
    UUID incidentId,
    Instant createdAt,
    Instant resolvedAt
) {
}
