package com.pulse.alert.service;

import com.pulse.alert.dto.AlertResponse;
import com.pulse.alert.dto.CreateAlertRequest;
import com.pulse.alert.entity.Alert;
import com.pulse.alert.exception.AlertNotFoundException;
import com.pulse.alert.repository.AlertRepository;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.exception.IncidentNotFoundException;
import com.pulse.incident.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final IncidentRepository incidentRepository;

    @Transactional
    public AlertResponse createAlert(CreateAlertRequest request) {
        Incident incident = incidentRepository.findById(request.incidentId())
            .orElseThrow(() -> new IncidentNotFoundException(request.incidentId()));

        Alert alert = new Alert(request.message(), request.severity(), incident);

        return toResponse(alertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlerts(UUID incidentId) {
        List<Alert> alerts = incidentId == null
            ? alertRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
            : alertRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId);

        return alerts.stream().map(this::toResponse).toList();
    }

    @Transactional
    public AlertResponse resolveAlert(UUID id) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new AlertNotFoundException(id));
        alert.resolve();

        return toResponse(alert);
    }

    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
            alert.getId(),
            alert.getMessage(),
            alert.getSeverity(),
            alert.getStatus(),
            alert.getIncident().getId(),
            alert.getCreatedAt(),
            alert.getResolvedAt()
        );
    }
}
