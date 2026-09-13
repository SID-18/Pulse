package com.pulse.event.service;

import com.pulse.event.dto.IncidentEventResponse;
import com.pulse.event.entity.IncidentEvent;
import com.pulse.event.entity.IncidentEventType;
import com.pulse.event.outbox.OutboxEvent;
import com.pulse.event.outbox.OutboxEventRepository;
import com.pulse.event.repository.IncidentEventRepository;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.exception.IncidentNotFoundException;
import com.pulse.incident.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class IncidentEventService {

    private final IncidentEventRepository incidentEventRepository;
    private final IncidentRepository incidentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final String incidentEventsTopic;

    public IncidentEventService(
        IncidentEventRepository incidentEventRepository,
        IncidentRepository incidentRepository,
        OutboxEventRepository outboxEventRepository,
        @Value("${pulse.kafka.topics.incident-events}") String incidentEventsTopic
    ) {
        this.incidentEventRepository = incidentEventRepository;
        this.incidentRepository = incidentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.incidentEventsTopic = incidentEventsTopic;
    }

    @Transactional
    public void record(
        Incident incident,
        IncidentEventType type,
        String message
    ) {
        IncidentEvent event = incidentEventRepository.save(
            new IncidentEvent(incident, type, message)
        );

        outboxEventRepository.save(new OutboxEvent(
            event.getId(),
            incident.getId(),
            incidentEventsTopic,
            event.getType(),
            event.getMessage(),
            event.getCreatedAt()
        ));
    }

    @Transactional(readOnly = true)
    public List<IncidentEventResponse> getEvents(UUID incidentId) {
        findIncidentById(incidentId);

        return incidentEventRepository
            .findByIncidentIdOrderByCreatedAtAsc(incidentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private Incident findIncidentById(UUID id) {
        return incidentRepository.findById(id)
            .orElseThrow(() -> new IncidentNotFoundException(id));
    }

    private IncidentEventResponse toResponse(IncidentEvent event) {
        return new IncidentEventResponse(
            event.getId(),
            event.getIncident().getId(),
            event.getType(),
            event.getMessage(),
            event.getCreatedAt()
        );
    }
}
