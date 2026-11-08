package com.pulse.runbook.controller;

import com.pulse.runbook.dto.CreateRunbookStepRequest;
import com.pulse.runbook.dto.RunbookStepResponse;
import com.pulse.runbook.service.RunbookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/services/{serviceId}/runbook-steps")
@RequiredArgsConstructor
public class RunbookController {

    private final RunbookService runbookService;

    @GetMapping
    public List<RunbookStepResponse> getSteps(@PathVariable UUID serviceId) {
        return runbookService.getSteps(serviceId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunbookStepResponse addStep(
        @PathVariable UUID serviceId,
        @Valid @RequestBody CreateRunbookStepRequest request
    ) {
        return runbookService.addStep(serviceId, request);
    }
}
