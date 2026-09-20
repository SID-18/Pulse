package com.pulse.ai.service;

import com.pulse.ai.client.IncidentRagClient;
import com.pulse.ai.dto.IncidentRagRecommendationResponse;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.entity.IncidentStatus;
import com.pulse.incident.repository.IncidentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentRagServiceTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private IncidentRagClient incidentRagClient;
    @InjectMocks private IncidentRagService incidentRagService;

    @Test
    void shouldIndexOnlyResolvedIncidents() {
        Incident resolvedIncident = incident(
            "Payment API failure", IncidentSeverity.CRITICAL, IncidentStatus.RESOLVED
        );
        when(incidentRepository.findAllByStatus(IncidentStatus.RESOLVED))
            .thenReturn(List.of(resolvedIncident));
        when(incidentRagClient.index(any())).thenReturn(1);

        var response = incidentRagService.indexResolvedIncidents();

        ArgumentCaptor<List> documents = ArgumentCaptor.forClass(List.class);
        verify(incidentRagClient).index(documents.capture());
        assertEquals(1, response.indexedIncidents());
        assertEquals(1, documents.getValue().size());
    }

    @Test
    void shouldReturnRecommendationForTheRequestedIncident() {
        UUID incidentId = UUID.randomUUID();
        Incident incident = incident(
            "Checkout latency spike", IncidentSeverity.HIGH, IncidentStatus.OPEN
        );
        ReflectionTestUtils.setField(incident, "id", incidentId);
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        when(incidentRagClient.analyze(any())).thenReturn(
            new IncidentRagRecommendationResponse(
                null, "qwen2.5:3b", "Monitor connection-pool usage.", List.of(), Instant.now()
            )
        );

        var response = incidentRagService.recommend(incidentId);

        assertEquals(incidentId, response.incidentId());
        assertEquals("Monitor connection-pool usage.", response.recommendation());
        assertNotNull(response.generatedAt());
    }

    private Incident incident(
        String title,
        IncidentSeverity severity,
        IncidentStatus status
    ) {
        Incident incident = new Incident(title, "Test incident description.", severity);
        ReflectionTestUtils.setField(incident, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(incident, "status", status);
        ReflectionTestUtils.setField(incident, "createdAt", Instant.now());
        if (status == IncidentStatus.RESOLVED) {
            ReflectionTestUtils.setField(incident, "resolvedAt", Instant.now());
        }
        return incident;
    }
}
