package com.pulse.alert.dto;

import com.pulse.alert.entity.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAlertRequest(

    @NotBlank
    @Size(max = 5_000)
    String message,

    @NotNull
    AlertSeverity severity,

    @NotNull
    UUID incidentId
) {
}
