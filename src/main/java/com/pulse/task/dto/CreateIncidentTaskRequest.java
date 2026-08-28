package com.pulse.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateIncidentTaskRequest(

    @NotBlank
    @Size(max = 255)
    String title,

    @Size(max = 5_000)
    String description,

    UUID assignedUserId
) {
}
