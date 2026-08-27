package com.pulse.incident.dto;

import com.pulse.incident.entity.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIncidentRequest(

    @NotBlank
    @Size(max = 255)
    String title,

    @Size(max = 5_000)
    String description,

    @NotNull
    IncidentSeverity severity
) {
}
