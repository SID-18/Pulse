package com.pulse.team.dto;

import java.util.UUID;

public record TeamResponse(
    UUID id,
    String name
) {
}
