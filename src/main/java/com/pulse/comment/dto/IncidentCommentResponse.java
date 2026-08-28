package com.pulse.comment.dto;

import java.time.Instant;
import java.util.UUID;

public record IncidentCommentResponse(
    UUID id,
    String content,
    UUID incidentId,
    UUID authorId,
    String authorName,
    Instant createdAt
) {
}
