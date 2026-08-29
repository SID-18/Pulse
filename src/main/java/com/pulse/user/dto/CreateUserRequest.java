package com.pulse.user.dto;

import com.pulse.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserRequest(

    @NotBlank
    @Size(max = 255)
    String name,

    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    @NotNull
    UserRole role,

    @NotBlank
    @Size(min = 12, max = 72)
    String password,

    @NotNull
    UUID teamId
) {
}
