package com.pulse.oncall.controller;

import com.pulse.oncall.dto.CreateOnCallAssignmentRequest;
import com.pulse.oncall.dto.OnCallAssignmentResponse;
import com.pulse.oncall.service.OnCallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/on-call")
@RequiredArgsConstructor
public class OnCallController {

    private final OnCallService onCallService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OnCallAssignmentResponse schedule(@Valid @RequestBody CreateOnCallAssignmentRequest request) {
        return onCallService.schedule(request);
    }

    @GetMapping("/active")
    public OnCallAssignmentResponse getActive(@RequestParam UUID teamId) {
        return onCallService.getActiveAssignment(teamId, Instant.now());
    }
}
