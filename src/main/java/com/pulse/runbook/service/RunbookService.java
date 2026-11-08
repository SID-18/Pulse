package com.pulse.runbook.service;

import com.pulse.event.entity.IncidentEventType;
import com.pulse.event.service.IncidentEventService;
import com.pulse.incident.entity.Incident;
import com.pulse.runbook.dto.CreateRunbookStepRequest;
import com.pulse.runbook.dto.RunbookStepResponse;
import com.pulse.runbook.entity.ServiceRunbookStep;
import com.pulse.runbook.repository.ServiceRunbookStepRepository;
import com.pulse.service.entity.MonitoredService;
import com.pulse.service.exception.MonitoredServiceNotFoundException;
import com.pulse.service.repository.MonitoredServiceRepository;
import com.pulse.task.entity.IncidentTask;
import com.pulse.task.repository.IncidentTaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class RunbookService {

    private final MonitoredServiceRepository monitoredServiceRepository;
    private final ServiceRunbookStepRepository runbookStepRepository;
    private final IncidentTaskRepository incidentTaskRepository;
    private final IncidentEventService incidentEventService;

    public RunbookService(
        MonitoredServiceRepository monitoredServiceRepository,
        ServiceRunbookStepRepository runbookStepRepository,
        IncidentTaskRepository incidentTaskRepository,
        IncidentEventService incidentEventService
    ) {
        this.monitoredServiceRepository = monitoredServiceRepository;
        this.runbookStepRepository = runbookStepRepository;
        this.incidentTaskRepository = incidentTaskRepository;
        this.incidentEventService = incidentEventService;
    }

    @Transactional
    public RunbookStepResponse addStep(UUID serviceId, CreateRunbookStepRequest request) {
        MonitoredService service = findService(serviceId);
        if (runbookStepRepository.existsByServiceIdAndStepOrder(serviceId, request.stepOrder())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "A runbook step already exists at this position.");
        }
        return toResponse(runbookStepRepository.save(new ServiceRunbookStep(
            service, request.stepOrder(), request.title(), request.description()
        )));
    }

    @Transactional(readOnly = true)
    public List<RunbookStepResponse> getSteps(UUID serviceId) {
        findService(serviceId);
        return runbookStepRepository.findByServiceIdOrderByStepOrderAsc(serviceId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void applyToIncident(Incident incident, MonitoredService service) {
        List<ServiceRunbookStep> steps = runbookStepRepository
            .findByServiceIdOrderByStepOrderAsc(service.getId());
        int createdTaskCount = 0;
        for (ServiceRunbookStep step : steps) {
            if (incidentTaskRepository.existsByIncidentIdAndTitle(incident.getId(), step.getTitle())) {
                continue;
            }
            incidentTaskRepository.save(new IncidentTask(
                step.getTitle(), step.getDescription(), incident, incident.getOwner()
            ));
            createdTaskCount++;
        }
        if (createdTaskCount > 0) {
            incidentEventService.record(
                incident,
                IncidentEventType.RUNBOOK_APPLIED,
                "Runbook applied: " + createdTaskCount + " response task(s) created."
            );
        }
    }

    private MonitoredService findService(UUID serviceId) {
        return monitoredServiceRepository.findById(serviceId)
            .orElseThrow(() -> new MonitoredServiceNotFoundException(serviceId));
    }

    private RunbookStepResponse toResponse(ServiceRunbookStep step) {
        return new RunbookStepResponse(
            step.getId(), step.getService().getId(), step.getStepOrder(),
            step.getTitle(), step.getDescription()
        );
    }
}
