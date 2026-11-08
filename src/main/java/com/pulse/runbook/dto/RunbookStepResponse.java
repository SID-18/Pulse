package com.pulse.runbook.dto;

import java.util.UUID;

public record RunbookStepResponse(
    UUID id,
    UUID serviceId,
    int stepOrder,
    String title,
    String description
) {
}
