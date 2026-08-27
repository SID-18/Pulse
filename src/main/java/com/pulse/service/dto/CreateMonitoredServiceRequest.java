package com.pulse.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMonitoredServiceRequest(

    @NotBlank
    @Size(max = 255)
    String name,

    @Size(max = 5_000)
    String description
) {
}
