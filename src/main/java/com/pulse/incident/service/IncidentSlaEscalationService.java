package com.pulse.incident.service;

import com.pulse.alert.entity.Alert;
import com.pulse.alert.entity.AlertSeverity;
import com.pulse.alert.entity.AlertStatus;
import com.pulse.alert.repository.AlertRepository;
import com.pulse.event.entity.IncidentEventType;
import com.pulse.event.repository.IncidentEventRepository;
import com.pulse.event.service.IncidentEventService;
import com.pulse.incident.dto.IncidentSlaResponse;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentStatus;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.team.entity.Team;
import com.pulse.user.entity.User;
import com.pulse.user.entity.UserRole;
import com.pulse.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class IncidentSlaEscalationService {

    static final String ACKNOWLEDGEMENT_BREACH_MESSAGE =
        "SLA acknowledgement target breached.";
    static final String RESOLUTION_BREACH_MESSAGE =
        "SLA resolution target breached.";
    static final String MANAGER_ESCALATION_MESSAGE =
        "Incident remains unacknowledged after the owner notification window.";

    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final IncidentEventRepository incidentEventRepository;
    private final UserRepository userRepository;
    private final IncidentEventService incidentEventService;
    private final IncidentSlaPolicy incidentSlaPolicy;
    private final Duration managerEscalationDelay;

    public IncidentSlaEscalationService(
        IncidentRepository incidentRepository,
        AlertRepository alertRepository,
        IncidentEventRepository incidentEventRepository,
        UserRepository userRepository,
        IncidentEventService incidentEventService,
        IncidentSlaPolicy incidentSlaPolicy,
        @Value("${pulse.sla.escalation.manager-delay:15m}") Duration managerEscalationDelay
    ) {
        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
        this.incidentEventRepository = incidentEventRepository;
        this.userRepository = userRepository;
        this.incidentEventService = incidentEventService;
        this.incidentSlaPolicy = incidentSlaPolicy;
        this.managerEscalationDelay = managerEscalationDelay;
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
            escalateToManagerIfStillUnacknowledged(incident);
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

    private void escalateToManagerIfStillUnacknowledged(Incident incident) {
        User owner = incident.getOwner();
        if (owner == null
            || owner.getRole() == UserRole.MANAGER
            || incidentEventRepository.existsByIncidentIdAndType(
                incident.getId(), IncidentEventType.SLA_MANAGER_ESCALATED
            )) {
            return;
        }

        incidentEventRepository.findFirstByIncidentIdAndTypeOrderByCreatedAtAsc(
                incident.getId(), IncidentEventType.SLA_ACKNOWLEDGEMENT_BREACHED
            )
            .filter(event -> event.getCreatedAt().plus(managerEscalationDelay).isBefore(Instant.now()))
            .flatMap(ignored -> managerFor(owner))
            .filter(manager -> !manager.getId().equals(owner.getId()))
            .ifPresent(manager -> incidentEventService.recordForRecipient(
                incident,
                IncidentEventType.SLA_MANAGER_ESCALATED,
                MANAGER_ESCALATION_MESSAGE,
                manager
            ));
    }

    private Optional<User> managerFor(User owner) {
        Team team = owner.getTeam();
        return team == null
            ? Optional.empty()
            : userRepository.findFirstByTeamIdAndRoleOrderByNameAsc(team.getId(), UserRole.MANAGER);
    }
}
