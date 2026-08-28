package com.pulse.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateIncidentCommentRequest(

    @NotBlank
    @Size(max = 5_000)
    String content,

    @NotNull
    UUID authorId
) {
}
