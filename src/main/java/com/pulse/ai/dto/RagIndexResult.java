package com.pulse.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RagIndexResult(@JsonProperty("indexed_count") int indexedCount) {
}
