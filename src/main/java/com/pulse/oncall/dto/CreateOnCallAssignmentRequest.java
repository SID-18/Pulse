package com.pulse.oncall.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateOnCallAssignmentRequest(
    @NotNull UUID teamId,
    @NotNull UUID userId,
    @NotNull Instant startsAt,
    @NotNull Instant endsAt
) {
}
