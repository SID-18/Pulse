package com.pulse.event.service;

import com.pulse.event.dto.IncidentEventResponse;
import com.pulse.event.domain.IncidentEventRecorded;
import com.pulse.event.entity.IncidentEvent;
import com.pulse.event.entity.IncidentEventType;
import com.pulse.event.repository.IncidentEventRepository;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.exception.IncidentNotFoundException;
import com.pulse.incident.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentEventService {

    private final IncidentEventRepository incidentEventRepository;
    private final IncidentRepository incidentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void record(
        Incident incident,
        IncidentEventType type,
        String message
    ) {
        IncidentEvent event = incidentEventRepository.save(
            new IncidentEvent(incident, type, message)
        );

        applicationEventPublisher.publishEvent(new IncidentEventRecorded(
            event.getId(),
            incident.getId(),
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
