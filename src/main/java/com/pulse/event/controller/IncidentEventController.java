package com.pulse.event.controller;

import com.pulse.event.dto.IncidentEventResponse;
import com.pulse.event.service.IncidentEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class IncidentEventController {

    private final IncidentEventService incidentEventService;

    @GetMapping("/api/incidents/{incidentId}/events")
    public List<IncidentEventResponse> getEvents(
        @PathVariable UUID incidentId
    ) {
        return incidentEventService.getEvents(incidentId);
    }
}
