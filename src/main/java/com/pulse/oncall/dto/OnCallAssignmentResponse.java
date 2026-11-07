package com.pulse.oncall.dto;

import java.time.Instant;
import java.util.UUID;

public record OnCallAssignmentResponse(
    UUID id,
    UUID teamId,
    String teamName,
    UUID userId,
    String userName,
    Instant startsAt,
    Instant endsAt
) {
}
