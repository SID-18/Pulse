package com.pulse.incident.entity;

import com.pulse.incident.exception.InvalidIncidentStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidentTest {

    @Test
    void shouldStartAsOpen() {
        Incident incident = newIncident();

        assertEquals(IncidentStatus.OPEN, incident.getStatus());
    }

    @Test
    void shouldAcknowledgeAnOpenIncident() {
        Incident incident = newIncident();

        incident.acknowledge();

        assertEquals(IncidentStatus.ACKNOWLEDGED, incident.getStatus());
    }

    @Test
    void shouldResolveAnAcknowledgedIncident() {
        Incident incident = newIncident();
        incident.acknowledge();

        incident.resolve();

        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
        assertNotNull(incident.getResolvedAt());
    }

    @Test
    void shouldRejectResolvingAnOpenIncident() {
        Incident incident = newIncident();

        assertThrows(
            InvalidIncidentStatusTransitionException.class,
            incident::resolve
        );
    }

    private Incident newIncident() {
        return new Incident(
            "Payment API failure",
            "Checkout requests return HTTP 500.",
            IncidentSeverity.CRITICAL
        );
    }
}
