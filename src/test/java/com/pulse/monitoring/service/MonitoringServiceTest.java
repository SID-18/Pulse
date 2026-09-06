package com.pulse.monitoring.service;

import com.pulse.alert.entity.Alert;
import com.pulse.alert.repository.AlertRepository;
import com.pulse.event.service.IncidentEventService;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.monitoring.dto.CreateHealthCheckRequest;
import com.pulse.monitoring.dto.ServiceHealthResponse;
import com.pulse.monitoring.entity.HealthStatus;
import com.pulse.monitoring.entity.ServiceHealthCheck;
import com.pulse.monitoring.repository.ServiceHealthCheckRepository;
import com.pulse.service.entity.MonitoredService;
import com.pulse.service.repository.MonitoredServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock private MonitoredServiceRepository monitoredServiceRepository;
    @Mock private ServiceHealthCheckRepository healthCheckRepository;
    @Mock private IncidentRepository incidentRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private IncidentEventService incidentEventService;
    @InjectMocks private MonitoringService monitoringService;

    @Test
    void shouldRecordAHealthyCheckWithoutCreatingAnIncident() {
        MonitoredService service = service();
        when(monitoredServiceRepository.findById(service.getId()))
            .thenReturn(Optional.of(service));
        when(healthCheckRepository.save(any(ServiceHealthCheck.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(incidentRepository.findFirstByServiceIdAndStatusInOrderByCreatedAtDesc(
            any(), any()
        )).thenReturn(Optional.empty());

        ServiceHealthResponse response = monitoringService.recordHealthCheck(
            service.getId(), new CreateHealthCheckRequest(180, new BigDecimal("0.10"))
        );

        assertEquals(HealthStatus.HEALTHY, response.status());
        verify(incidentRepository, never()).save(any(Incident.class));
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void shouldCreateOneIncidentAndAlertForANewServiceOutage() {
        MonitoredService service = service();
        UUID incidentId = UUID.randomUUID();
        when(monitoredServiceRepository.findById(service.getId()))
            .thenReturn(Optional.of(service));
        when(healthCheckRepository.save(any(ServiceHealthCheck.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(incidentRepository.findFirstByServiceIdAndStatusInOrderByCreatedAtDesc(
            any(), any()
        )).thenReturn(Optional.empty());
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
            Incident incident = invocation.getArgument(0);
            ReflectionTestUtils.setField(incident, "id", incidentId);
            return incident;
        });
        when(alertRepository.save(any(Alert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ServiceHealthResponse response = monitoringService.recordHealthCheck(
            service.getId(), new CreateHealthCheckRequest(2_500, new BigDecimal("7.50"))
        );

        assertEquals(HealthStatus.DOWN, response.status());
        assertEquals(incidentId, response.activeIncidentId());
        verify(incidentRepository).save(any(Incident.class));
        verify(alertRepository).save(any(Alert.class));
    }

    private MonitoredService service() {
        MonitoredService service = new MonitoredService("Payment API", "Payments");
        ReflectionTestUtils.setField(service, "id", UUID.randomUUID());
        return service;
    }
}
