package com.pulse.incident.service;

import com.pulse.incident.dto.CreateIncidentRequest;
import com.pulse.incident.dto.IncidentResponse;
import com.pulse.incident.dto.IncidentSortField;
import com.pulse.incident.dto.PagedResponse;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentStatus;
import com.pulse.incident.exception.IncidentNotFoundException;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.service.entity.MonitoredService;
import com.pulse.service.exception.MonitoredServiceNotFoundException;
import com.pulse.service.repository.MonitoredServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final MonitoredServiceRepository monitoredServiceRepository;

    @Transactional
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        Incident incident = new Incident(
            request.title(),
            request.description(),
            request.severity()
        );

        Incident savedIncident = incidentRepository.save(incident);

        return toResponse(savedIncident);
    }

    @Transactional(readOnly = true)
    public PagedResponse<IncidentResponse> getIncidents(
        IncidentStatus status,
        int page,
        int size,
        IncidentSortField sortField,
        Sort.Direction direction
    ) {
        PageRequest pageRequest = PageRequest.of(
            page,
            size,
            Sort.by(direction, sortField.property())
        );
        Page<Incident> incidents = status == null
            ? incidentRepository.findAll(pageRequest)
            : incidentRepository.findByStatus(status, pageRequest);

        return PagedResponse.from(incidents, this::toResponse);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncident(UUID id) {
        return toResponse(findIncidentById(id));
    }

    @Transactional
    public IncidentResponse acknowledgeIncident(UUID id) {
        Incident incident = findIncidentById(id);
        incident.acknowledge();

        return toResponse(incident);
    }

    @Transactional
    public IncidentResponse resolveIncident(UUID id) {
        Incident incident = findIncidentById(id);
        incident.resolve();

        return toResponse(incident);
    }

    @Transactional
    public IncidentResponse assignService(UUID incidentId, UUID serviceId) {
        Incident incident = findIncidentById(incidentId);

        MonitoredService service = monitoredServiceRepository
            .findById(serviceId)
            .orElseThrow(
                () -> new MonitoredServiceNotFoundException(serviceId)
            );

        incident.assignService(service);

        return toResponse(incident);
    }

    private Incident findIncidentById(UUID id) {
        return incidentRepository.findById(id)
            .orElseThrow(() -> new IncidentNotFoundException(id));
    }

    private IncidentResponse toResponse(Incident incident) {
        MonitoredService assignedService = incident.getService();

        return new IncidentResponse(
            incident.getId(),
            incident.getTitle(),
            incident.getDescription(),
            incident.getSeverity(),
            incident.getStatus(),
            assignedService == null ? null : assignedService.getId(),
            incident.getCreatedAt(),
            incident.getResolvedAt()
        );
    }
}
