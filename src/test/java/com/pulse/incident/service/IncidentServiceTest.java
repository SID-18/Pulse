package com.pulse.incident.service;

import com.pulse.incident.dto.CreateIncidentRequest;
import com.pulse.incident.dto.IncidentResponse;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.entity.IncidentStatus;
import com.pulse.incident.exception.IncidentNotFoundException;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.service.entity.MonitoredService;
import com.pulse.service.exception.MonitoredServiceNotFoundException;
import com.pulse.service.repository.MonitoredServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private MonitoredServiceRepository monitoredServiceRepository;

    @InjectMocks
    private IncidentService incidentService;

    @Test
    void shouldCreateAnOpenIncident() {
        CreateIncidentRequest request = new CreateIncidentRequest(
            "Payment API failure",
            "Checkout requests return HTTP 500.",
            IncidentSeverity.CRITICAL
        );
        when(incidentRepository.save(any(Incident.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        IncidentResponse response = incidentService.createIncident(request);

        assertEquals("Payment API failure", response.title());
        assertEquals(IncidentStatus.OPEN, response.status());
        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void shouldAcknowledgeAnExistingIncident() {
        UUID id = UUID.randomUUID();
        Incident incident = new Incident(
            "Payment API failure",
            "Checkout requests return HTTP 500.",
            IncidentSeverity.CRITICAL
        );
        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));

        IncidentResponse response = incidentService.acknowledgeIncident(id);

        assertEquals(IncidentStatus.ACKNOWLEDGED, response.status());
        verify(incidentRepository).findById(id);
    }

    @Test
    void shouldThrowWhenIncidentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(incidentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
            IncidentNotFoundException.class,
            () -> incidentService.getIncident(id)
        );
    }

    @Test
    void shouldAssignAServiceToAnIncident() {
        UUID incidentId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        Incident incident = new Incident(
            "Payment API failure",
            "Checkout requests return HTTP 500.",
            IncidentSeverity.CRITICAL
        );
        MonitoredService service = new MonitoredService(
            "Payment API",
            "Handles checkout payment processing."
        );
        ReflectionTestUtils.setField(service, "id", serviceId);
        when(incidentRepository.findById(incidentId))
            .thenReturn(Optional.of(incident));
        when(monitoredServiceRepository.findById(serviceId))
            .thenReturn(Optional.of(service));

        IncidentResponse response = incidentService.assignService(
            incidentId,
            serviceId
        );

        assertEquals(service.getId(), response.serviceId());
        assertEquals(service, incident.getService());
    }

    @Test
    void shouldThrowWhenAssigningServiceToMissingIncident() {
        UUID incidentId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        when(incidentRepository.findById(incidentId))
            .thenReturn(Optional.empty());

        assertThrows(
            IncidentNotFoundException.class,
            () -> incidentService.assignService(incidentId, serviceId)
        );
    }

    @Test
    void shouldThrowWhenAssigningMissingService() {
        UUID incidentId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        Incident incident = new Incident(
            "Payment API failure",
            "Checkout requests return HTTP 500.",
            IncidentSeverity.CRITICAL
        );
        when(incidentRepository.findById(incidentId))
            .thenReturn(Optional.of(incident));
        when(monitoredServiceRepository.findById(serviceId))
            .thenReturn(Optional.empty());

        assertThrows(
            MonitoredServiceNotFoundException.class,
            () -> incidentService.assignService(incidentId, serviceId)
        );
    }
}
