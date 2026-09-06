package com.pulse.monitoring.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateHealthCheckRequest(
    @NotNull @Min(0) Integer latencyMs,
    @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal errorRatePercent
) {
}
