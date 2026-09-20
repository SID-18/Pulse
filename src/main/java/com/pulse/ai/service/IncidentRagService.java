package com.pulse.ai.service;

import com.pulse.ai.client.IncidentRagClient;
import com.pulse.ai.dto.IncidentRagRecommendationResponse;
import com.pulse.ai.dto.RagIncidentDocument;
import com.pulse.ai.dto.RagIndexResponse;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentStatus;
import com.pulse.incident.exception.IncidentNotFoundException;
import com.pulse.incident.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentRagService {

    private final IncidentRepository incidentRepository;
    private final IncidentRagClient incidentRagClient;

    public IncidentRagService(
        IncidentRepository incidentRepository,
        IncidentRagClient incidentRagClient
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentRagClient = incidentRagClient;
    }

    @Transactional(readOnly = true)
    public RagIndexResponse indexResolvedIncidents() {
        List<RagIncidentDocument> incidents = incidentRepository
            .findAllByStatus(IncidentStatus.RESOLVED)
            .stream()
            .map(this::toDocument)
            .toList();

        return new RagIndexResponse(incidentRagClient.index(incidents), Instant.now());
    }

    @Transactional(readOnly = true)
    public IncidentRagRecommendationResponse recommend(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new IncidentNotFoundException(incidentId));
        IncidentRagRecommendationResponse response = incidentRagClient.analyze(
            toDocument(incident)
        );

        return new IncidentRagRecommendationResponse(
            incident.getId(),
            response.model(),
            response.recommendation(),
            response.similarIncidents(),
            Instant.now()
        );
    }

    private RagIncidentDocument toDocument(Incident incident) {
        return new RagIncidentDocument(
            incident.getId(),
            incident.getTitle(),
            incident.getDescription(),
            incident.getSeverity().name(),
            incident.getStatus().name(),
            incident.getService() == null ? null : incident.getService().getName(),
            incident.getCreatedAt(),
            incident.getResolvedAt()
        );
    }
}
