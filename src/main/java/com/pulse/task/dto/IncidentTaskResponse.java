package com.pulse.task.dto;

import com.pulse.task.entity.IncidentTaskStatus;

import java.time.Instant;
import java.util.UUID;

public record IncidentTaskResponse(
    UUID id,
    String title,
    String description,
    IncidentTaskStatus status,
    UUID incidentId,
    UUID assignedUserId,
    String assignedUserName,
    Instant createdAt,
    Instant completedAt
) {
}
