package com.pulse.ai.dto;

import java.time.Instant;
import java.util.UUID;

public record IncidentAiSummaryResponse(
    UUID incidentId,
    String model,
    String summary,
    Instant generatedAt
) {
}
