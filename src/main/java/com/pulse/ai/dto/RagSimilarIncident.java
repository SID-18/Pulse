package com.pulse.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record RagSimilarIncident(
    UUID id,
    String title,
    String severity,
    String status,
    @JsonProperty("service_name") String serviceName,
    double similarity
) {
}
