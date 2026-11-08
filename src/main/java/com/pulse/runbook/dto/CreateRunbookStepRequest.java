package com.pulse.runbook.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRunbookStepRequest(
    @NotNull @Min(1) Integer stepOrder,
    @NotBlank @Size(max = 255) String title,
    @Size(max = 5_000) String description
) {
}
