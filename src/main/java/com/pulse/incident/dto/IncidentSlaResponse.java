package com.pulse.incident.dto;

import java.time.Instant;
import java.io.Serializable;

public record IncidentSlaResponse(
    Instant acknowledgementDueAt,
    Instant resolutionDueAt,
    String acknowledgementStatus,
    String resolutionStatus
) implements Serializable {
}
