package com.pulse.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IncidentRagRecommendationResponse(
    UUID incidentId,
    String model,
    String recommendation,
    @JsonProperty("similar_incidents") List<RagSimilarIncident> similarIncidents,
    Instant generatedAt
) {
}
