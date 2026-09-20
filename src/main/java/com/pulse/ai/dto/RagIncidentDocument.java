package com.pulse.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record RagIncidentDocument(
    UUID id,
    String title,
    String description,
    String severity,
    String status,
    @JsonProperty("service_name") String serviceName,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("resolved_at") Instant resolvedAt
) {
}
