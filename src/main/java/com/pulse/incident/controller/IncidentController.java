package com.pulse.incident.controller;

import com.pulse.incident.dto.CreateIncidentRequest;
import com.pulse.incident.dto.IncidentResponse;
import com.pulse.incident.dto.IncidentSortField;
import com.pulse.incident.dto.PagedResponse;
import com.pulse.incident.entity.IncidentStatus;
import com.pulse.incident.service.IncidentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Validated
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse createIncident(
        @Valid @RequestBody CreateIncidentRequest request
    ) {
        return incidentService.createIncident(request);
    }

    @GetMapping
    public PagedResponse<IncidentResponse> getIncidents(
        @RequestParam(required = false) IncidentStatus status,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "CREATED_AT") IncidentSortField sortBy,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return incidentService.getIncidents(
            status,
            page,
            size,
            sortBy,
            direction
        );
    }

    @GetMapping("/{id}")
    public IncidentResponse getIncident(@PathVariable UUID id) {
        return incidentService.getIncident(id);
    }

    @PatchMapping("/{id}/acknowledge")
    public IncidentResponse acknowledgeIncident(@PathVariable UUID id) {
        return incidentService.acknowledgeIncident(id);
    }

    @PatchMapping("/{id}/resolve")
    public IncidentResponse resolveIncident(@PathVariable UUID id) {
        return incidentService.resolveIncident(id);
    }

    @PatchMapping("/{incidentId}/service/{serviceId}")
    public IncidentResponse assignService(
        @PathVariable UUID incidentId,
        @PathVariable UUID serviceId
    ) {
        return incidentService.assignService(incidentId, serviceId);
    }
}
