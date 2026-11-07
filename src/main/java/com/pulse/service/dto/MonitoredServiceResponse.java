package com.pulse.service.dto;

import java.time.Instant;
import java.util.UUID;

public record MonitoredServiceResponse(
    UUID id,
    String name,
    String description,
    Instant createdAt,
    UUID teamId,
    String teamName
) {
    public MonitoredServiceResponse(UUID id, String name, String description, Instant createdAt) {
        this(id, name, description, createdAt, null, null);
    }
}
