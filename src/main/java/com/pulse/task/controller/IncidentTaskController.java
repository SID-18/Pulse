package com.pulse.task.controller;

import com.pulse.task.dto.CreateIncidentTaskRequest;
import com.pulse.task.dto.IncidentTaskResponse;
import com.pulse.task.service.IncidentTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
public class IncidentTaskController {

    private final IncidentTaskService incidentTaskService;

    @PostMapping("/api/incidents/{incidentId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentTaskResponse createTask(
        @PathVariable UUID incidentId,
        @Valid @RequestBody CreateIncidentTaskRequest request
    ) {
        return incidentTaskService.createTask(incidentId, request);
    }

    @GetMapping("/api/incidents/{incidentId}/tasks")
    public List<IncidentTaskResponse> getTasks(
        @PathVariable UUID incidentId
    ) {
        return incidentTaskService.getTasks(incidentId);
    }

    @PatchMapping("/api/tasks/{id}/start")
    public IncidentTaskResponse startTask(@PathVariable UUID id) {
        return incidentTaskService.startTask(id);
    }

    @PatchMapping("/api/tasks/{id}/complete")
    public IncidentTaskResponse completeTask(@PathVariable UUID id) {
        return incidentTaskService.completeTask(id);
    }
}
