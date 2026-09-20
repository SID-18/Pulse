package com.pulse.ai.controller;

import com.pulse.ai.dto.IncidentAiSummaryResponse;
import com.pulse.ai.dto.IncidentRagRecommendationResponse;
import com.pulse.ai.dto.RagIndexResponse;
import com.pulse.ai.service.IncidentRagService;
import com.pulse.ai.service.IncidentAiSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentAiController {

    private final IncidentAiSummaryService incidentAiSummaryService;
    private final IncidentRagService incidentRagService;

    public IncidentAiController(
        IncidentAiSummaryService incidentAiSummaryService,
        IncidentRagService incidentRagService
    ) {
        this.incidentAiSummaryService = incidentAiSummaryService;
        this.incidentRagService = incidentRagService;
    }

    @GetMapping("/{incidentId}/ai-summary")
    public IncidentAiSummaryResponse summarizeIncident(
        @PathVariable UUID incidentId
    ) {
        return incidentAiSummaryService.summarize(incidentId);
    }

    @GetMapping("/{incidentId}/ai-recommendation")
    public IncidentRagRecommendationResponse recommendForIncident(
        @PathVariable UUID incidentId
    ) {
        return incidentRagService.recommend(incidentId);
    }

    @PostMapping("/ai/reindex-resolved")
    public RagIndexResponse indexResolvedIncidents() {
        return incidentRagService.indexResolvedIncidents();
    }
}
