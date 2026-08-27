package com.pulse.service.dto;

import java.time.Instant;
import java.util.UUID;

public record MonitoredServiceResponse(
    UUID id,
    String name,
    String description,
    Instant createdAt
) {
}
