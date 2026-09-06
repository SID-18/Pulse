package com.pulse.ai.controller;

import com.pulse.ai.dto.IncidentAiSummaryResponse;
import com.pulse.ai.service.IncidentAiSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentAiController {

    private final IncidentAiSummaryService incidentAiSummaryService;

    public IncidentAiController(IncidentAiSummaryService incidentAiSummaryService) {
        this.incidentAiSummaryService = incidentAiSummaryService;
    }

    @GetMapping("/{incidentId}/ai-summary")
    public IncidentAiSummaryResponse summarizeIncident(
        @PathVariable UUID incidentId
    ) {
        return incidentAiSummaryService.summarize(incidentId);
    }
}
