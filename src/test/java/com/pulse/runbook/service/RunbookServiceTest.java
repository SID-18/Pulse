package com.pulse.runbook.service;

import com.pulse.event.entity.IncidentEventType;
import com.pulse.event.service.IncidentEventService;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.runbook.dto.CreateRunbookStepRequest;
import com.pulse.runbook.dto.RunbookStepResponse;
import com.pulse.runbook.entity.ServiceRunbookStep;
import com.pulse.runbook.repository.ServiceRunbookStepRepository;
import com.pulse.service.entity.MonitoredService;
import com.pulse.service.repository.MonitoredServiceRepository;
import com.pulse.task.entity.IncidentTask;
import com.pulse.task.repository.IncidentTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RunbookServiceTest {

    @Mock private MonitoredServiceRepository monitoredServiceRepository;
    @Mock private ServiceRunbookStepRepository runbookStepRepository;
    @Mock private IncidentTaskRepository incidentTaskRepository;
    @Mock private IncidentEventService incidentEventService;

    @Test
    void shouldCreateOrderedRunbookStep() {
        MonitoredService service = service();
        when(monitoredServiceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        when(runbookStepRepository.save(any(ServiceRunbookStep.class)))
            .thenAnswer(invocation -> {
                ServiceRunbookStep step = invocation.getArgument(0);
                ReflectionTestUtils.setField(step, "id", UUID.randomUUID());
                return step;
            });

        RunbookStepResponse result = runbookService().addStep(service.getId(),
            new CreateRunbookStepRequest(1, "Check dependencies", "Inspect database and Kafka health."));

        assertEquals(1, result.stepOrder());
        assertEquals("Check dependencies", result.title());
    }

    @Test
    void shouldCreateIncidentTasksFromServiceRunbookOnlyOnce() {
        MonitoredService service = service();
        Incident incident = new Incident("Payment delay", "Checkout is slow.", IncidentSeverity.HIGH);
        ReflectionTestUtils.setField(incident, "id", UUID.randomUUID());
        ServiceRunbookStep first = new ServiceRunbookStep(service, 1, "Check dependencies", "Inspect dependencies.");
        ServiceRunbookStep second = new ServiceRunbookStep(service, 2, "Verify recovery", "Confirm latency is normal.");
        when(runbookStepRepository.findByServiceIdOrderByStepOrderAsc(service.getId()))
            .thenReturn(List.of(first, second));
        when(incidentTaskRepository.existsByIncidentIdAndTitle(incident.getId(), "Check dependencies"))
            .thenReturn(false);
        when(incidentTaskRepository.existsByIncidentIdAndTitle(incident.getId(), "Verify recovery"))
            .thenReturn(true);

        runbookService().applyToIncident(incident, service);

        ArgumentCaptor<IncidentTask> task = ArgumentCaptor.forClass(IncidentTask.class);
        verify(incidentTaskRepository).save(task.capture());
        assertEquals("Check dependencies", task.getValue().getTitle());
        verify(incidentEventService).record(
            eq(incident), eq(IncidentEventType.RUNBOOK_APPLIED),
            eq("Runbook applied: 1 response task(s) created.")
        );
    }

    private RunbookService runbookService() {
        return new RunbookService(monitoredServiceRepository, runbookStepRepository,
            incidentTaskRepository, incidentEventService);
    }

    private MonitoredService service() {
        MonitoredService service = new MonitoredService("Payments", "Checkout payment processing.");
        ReflectionTestUtils.setField(service, "id", UUID.randomUUID());
        return service;
    }
}
