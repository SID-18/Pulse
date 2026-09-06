package com.pulse.monitoring.dto;

import com.pulse.monitoring.entity.HealthStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ServiceHealthResponse(
    UUID serviceId,
    String serviceName,
    HealthStatus status,
    Integer latencyMs,
    BigDecimal errorRatePercent,
    Instant checkedAt,
    UUID activeIncidentId
) {
}
