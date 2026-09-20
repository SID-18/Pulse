package com.pulse.ai.dto;

import java.time.Instant;

public record RagIndexResponse(int indexedIncidents, Instant indexedAt) {
}
