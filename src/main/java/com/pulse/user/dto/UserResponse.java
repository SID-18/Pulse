package com.pulse.user.dto;

import com.pulse.user.entity.UserRole;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    UserRole role,
    UUID teamId,
    String teamName
) {
}
