package com.pulse.incident.service;

import com.pulse.incident.dto.IncidentSlaResponse;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.entity.IncidentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class IncidentSlaPolicy {

    private final Duration criticalAcknowledgement;
    private final Duration criticalResolution;
    private final Duration highAcknowledgement;
    private final Duration highResolution;
    private final Duration mediumAcknowledgement;
    private final Duration mediumResolution;
    private final Duration lowAcknowledgement;
    private final Duration lowResolution;

    public IncidentSlaPolicy(
        @Value("${pulse.sla.critical.acknowledgement:15m}") Duration criticalAcknowledgement,
        @Value("${pulse.sla.critical.resolution:4h}") Duration criticalResolution,
        @Value("${pulse.sla.high.acknowledgement:30m}") Duration highAcknowledgement,
        @Value("${pulse.sla.high.resolution:8h}") Duration highResolution,
        @Value("${pulse.sla.medium.acknowledgement:2h}") Duration mediumAcknowledgement,
        @Value("${pulse.sla.medium.resolution:24h}") Duration mediumResolution,
        @Value("${pulse.sla.low.acknowledgement:8h}") Duration lowAcknowledgement,
        @Value("${pulse.sla.low.resolution:72h}") Duration lowResolution
    ) {
        this.criticalAcknowledgement = criticalAcknowledgement;
        this.criticalResolution = criticalResolution;
        this.highAcknowledgement = highAcknowledgement;
        this.highResolution = highResolution;
        this.mediumAcknowledgement = mediumAcknowledgement;
        this.mediumResolution = mediumResolution;
        this.lowAcknowledgement = lowAcknowledgement;
        this.lowResolution = lowResolution;
    }

    public IncidentSlaResponse evaluate(Incident incident) {
        Duration acknowledgementTarget = acknowledgementTarget(incident.getSeverity());
        Duration resolutionTarget = resolutionTarget(incident.getSeverity());
        Instant startedAt = incident.getCreatedAt() == null ? Instant.now() : incident.getCreatedAt();
        Instant acknowledgementDueAt = startedAt.plus(acknowledgementTarget);
        Instant resolutionDueAt = startedAt.plus(resolutionTarget);

        return new IncidentSlaResponse(
            acknowledgementDueAt,
            resolutionDueAt,
            acknowledgementStatus(incident, acknowledgementDueAt),
            isBreached(incident.getResolvedAt(), resolutionDueAt) ? "BREACHED" : "ON_TRACK"
        );
    }

    private String acknowledgementStatus(Incident incident, Instant dueAt) {
        if (incident.getAcknowledgedAt() == null
            && incident.getStatus() != IncidentStatus.OPEN) {
            return "NOT_RECORDED";
        }
        return isBreached(incident.getAcknowledgedAt(), dueAt)
            ? "BREACHED"
            : "ON_TRACK";
    }

    private boolean isBreached(Instant completedAt, Instant dueAt) {
        Instant comparisonTime = completedAt == null ? Instant.now() : completedAt;
        return comparisonTime.isAfter(dueAt);
    }

    private Duration acknowledgementTarget(IncidentSeverity severity) {
        return switch (severity) {
            case CRITICAL -> criticalAcknowledgement;
            case HIGH -> highAcknowledgement;
            case MEDIUM -> mediumAcknowledgement;
            case LOW -> lowAcknowledgement;
        };
    }

    private Duration resolutionTarget(IncidentSeverity severity) {
        return switch (severity) {
            case CRITICAL -> criticalResolution;
            case HIGH -> highResolution;
            case MEDIUM -> mediumResolution;
            case LOW -> lowResolution;
        };
    }
}
