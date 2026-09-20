package com.pulse.incident.service;

import com.pulse.incident.dto.IncidentSlaResponse;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncidentSlaPolicyTest {

    private final IncidentSlaPolicy policy = new IncidentSlaPolicy(
        Duration.ofMinutes(15), Duration.ofHours(4),
        Duration.ofMinutes(30), Duration.ofHours(8),
        Duration.ofHours(2), Duration.ofHours(24),
        Duration.ofHours(8), Duration.ofHours(72)
    );

    @Test
    void shouldCalculateOnTrackTargetsForANewCriticalIncident() {
        Instant createdAt = Instant.now().minus(Duration.ofMinutes(10));
        Incident incident = incidentCreatedAt(IncidentSeverity.CRITICAL, createdAt);

        IncidentSlaResponse sla = policy.evaluate(incident);

        assertEquals(createdAt.plus(Duration.ofMinutes(15)), sla.acknowledgementDueAt());
        assertEquals(createdAt.plus(Duration.ofHours(4)), sla.resolutionDueAt());
        assertEquals("ON_TRACK", sla.acknowledgementStatus());
        assertEquals("ON_TRACK", sla.resolutionStatus());
    }

    @Test
    void shouldMarkAnOverdueAcknowledgementAsBreached() {
        Incident incident = incidentCreatedAt(
            IncidentSeverity.HIGH,
            Instant.now().minus(Duration.ofMinutes(31))
        );

        IncidentSlaResponse sla = policy.evaluate(incident);

        assertEquals("BREACHED", sla.acknowledgementStatus());
        assertEquals("ON_TRACK", sla.resolutionStatus());
    }

    private Incident incidentCreatedAt(IncidentSeverity severity, Instant createdAt) {
        Incident incident = new Incident("Payment API failure", "Checkout requests fail.", severity);
        ReflectionTestUtils.setField(incident, "createdAt", createdAt);
        return incident;
    }
}
