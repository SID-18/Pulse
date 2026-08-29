package com.pulse.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateIncidentCommentRequest(

    @NotBlank
    @Size(max = 5_000)
    String content
) {
}
