package com.pulse.comment.controller;

import com.pulse.comment.dto.CreateIncidentCommentRequest;
import com.pulse.comment.dto.IncidentCommentResponse;
import com.pulse.comment.service.IncidentCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class IncidentCommentController {

    private final IncidentCommentService incidentCommentService;

    @PostMapping("/api/incidents/{incidentId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentCommentResponse createComment(
        @PathVariable UUID incidentId,
        @Valid @RequestBody CreateIncidentCommentRequest request,
        Authentication authentication
    ) {
        return incidentCommentService.createComment(
            incidentId,
            authentication.getName(),
            request
        );
    }

    @GetMapping("/api/incidents/{incidentId}/comments")
    public List<IncidentCommentResponse> getComments(
        @PathVariable UUID incidentId
    ) {
        return incidentCommentService.getComments(incidentId);
    }
}
