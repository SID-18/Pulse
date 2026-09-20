package com.pulse.incident.service;

import com.pulse.alert.entity.Alert;
import com.pulse.alert.entity.AlertSeverity;
import com.pulse.alert.entity.AlertStatus;
import com.pulse.alert.repository.AlertRepository;
import com.pulse.event.entity.IncidentEventType;
import com.pulse.event.service.IncidentEventService;
import com.pulse.incident.dto.IncidentSlaResponse;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentStatus;
import com.pulse.incident.repository.IncidentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentSlaEscalationService {

    static final String ACKNOWLEDGEMENT_BREACH_MESSAGE =
        "SLA acknowledgement target breached.";
    static final String RESOLUTION_BREACH_MESSAGE =
        "SLA resolution target breached.";

    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final IncidentEventService incidentEventService;
    private final IncidentSlaPolicy incidentSlaPolicy;

    public IncidentSlaEscalationService(
        IncidentRepository incidentRepository,
        AlertRepository alertRepository,
        IncidentEventService incidentEventService,
        IncidentSlaPolicy incidentSlaPolicy
    ) {
        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
        this.incidentEventService = incidentEventService;
        this.incidentSlaPolicy = incidentSlaPolicy;
    }

    @Scheduled(fixedDelayString = "${pulse.sla.escalation.fixed-delay-ms:60000}")
    @Transactional
    public void escalateBreachedIncidents() {
        incidentRepository.findAllByStatus(IncidentStatus.OPEN)
            .forEach(this::escalateOpenIncident);
        incidentRepository.findAllByStatus(IncidentStatus.ACKNOWLEDGED)
            .forEach(this::escalateAcknowledgedIncident);
    }

    private void escalateOpenIncident(Incident incident) {
        IncidentSlaResponse sla = incidentSlaPolicy.evaluate(incident);
        if ("BREACHED".equals(sla.acknowledgementStatus())) {
            createEscalationIfMissing(
                incident,
                ACKNOWLEDGEMENT_BREACH_MESSAGE,
                IncidentEventType.SLA_ACKNOWLEDGEMENT_BREACHED
            );
        }
        if ("BREACHED".equals(sla.resolutionStatus())) {
            createEscalationIfMissing(
                incident,
                RESOLUTION_BREACH_MESSAGE,
                IncidentEventType.SLA_RESOLUTION_BREACHED
            );
        }
    }

    private void escalateAcknowledgedIncident(Incident incident) {
        IncidentSlaResponse sla = incidentSlaPolicy.evaluate(incident);
        if ("BREACHED".equals(sla.resolutionStatus())) {
            createEscalationIfMissing(
                incident,
                RESOLUTION_BREACH_MESSAGE,
                IncidentEventType.SLA_RESOLUTION_BREACHED
            );
        }
    }

    private void createEscalationIfMissing(
        Incident incident,
        String alertMessage,
        IncidentEventType eventType
    ) {
        if (alertRepository.existsByIncidentIdAndMessageAndStatus(
            incident.getId(), alertMessage, AlertStatus.FIRING
        )) {
            return;
        }

        alertRepository.save(new Alert(
            alertMessage,
            AlertSeverity.valueOf(incident.getSeverity().name()),
            incident
        ));
        incidentEventService.record(incident, eventType, alertMessage);
    }
}
