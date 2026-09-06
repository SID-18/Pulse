package com.pulse.monitoring.service;

import com.pulse.alert.entity.Alert;
import com.pulse.alert.entity.AlertSeverity;
import com.pulse.alert.repository.AlertRepository;
import com.pulse.event.entity.IncidentEventType;
import com.pulse.event.service.IncidentEventService;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.entity.IncidentStatus;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.monitoring.dto.CreateHealthCheckRequest;
import com.pulse.monitoring.dto.ServiceHealthResponse;
import com.pulse.monitoring.entity.HealthStatus;
import com.pulse.monitoring.entity.ServiceHealthCheck;
import com.pulse.monitoring.repository.ServiceHealthCheckRepository;
import com.pulse.service.entity.MonitoredService;
import com.pulse.service.exception.MonitoredServiceNotFoundException;
import com.pulse.service.repository.MonitoredServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MonitoringService {

    private static final int DEGRADED_LATENCY_MS = 750;
    private static final int DOWN_LATENCY_MS = 2_000;
    private static final BigDecimal DEGRADED_ERROR_RATE = new BigDecimal("1.00");
    private static final BigDecimal DOWN_ERROR_RATE = new BigDecimal("5.00");

    private final MonitoredServiceRepository monitoredServiceRepository;
    private final ServiceHealthCheckRepository healthCheckRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final IncidentEventService incidentEventService;

    @Transactional(readOnly = true)
    public List<ServiceHealthResponse> getServiceHealth() {
        return monitoredServiceRepository.findAll(Sort.by("name"))
            .stream()
            .map(service -> healthCheckRepository
                .findFirstByServiceIdOrderByCheckedAtDesc(service.getId())
                .map(check -> toResponse(check, findActiveIncident(service.getId())))
                .orElseGet(() -> {
                    Incident activeIncident = findActiveIncident(service.getId());
                    return new ServiceHealthResponse(
                        service.getId(), service.getName(), HealthStatus.UNKNOWN,
                        null, null, null,
                        activeIncident == null ? null : activeIncident.getId()
                    );
                }))
            .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = "incidentPages", allEntries = true)
    public ServiceHealthResponse recordHealthCheck(
        UUID serviceId,
        CreateHealthCheckRequest request
    ) {
        MonitoredService service = monitoredServiceRepository.findById(serviceId)
            .orElseThrow(() -> new MonitoredServiceNotFoundException(serviceId));
        HealthStatus status = determineStatus(
            request.latencyMs(), request.errorRatePercent()
        );
        ServiceHealthCheck check = healthCheckRepository.save(new ServiceHealthCheck(
            service, status, request.latencyMs(), request.errorRatePercent()
        ));

        Incident activeIncident = status == HealthStatus.DOWN
            ? findOrCreateActiveIncident(service, check)
            : findActiveIncident(serviceId);

        return toResponse(check, activeIncident);
    }

    private HealthStatus determineStatus(int latencyMs, BigDecimal errorRatePercent) {
        if (latencyMs >= DOWN_LATENCY_MS
            || errorRatePercent.compareTo(DOWN_ERROR_RATE) >= 0) {
            return HealthStatus.DOWN;
        }
        if (latencyMs >= DEGRADED_LATENCY_MS
            || errorRatePercent.compareTo(DEGRADED_ERROR_RATE) >= 0) {
            return HealthStatus.DEGRADED;
        }
        return HealthStatus.HEALTHY;
    }

    private Incident findOrCreateActiveIncident(
        MonitoredService service,
        ServiceHealthCheck check
    ) {
        Incident activeIncident = findActiveIncident(service.getId());
        if (activeIncident != null) {
            return activeIncident;
        }

        Incident incident = new Incident(
            "Monitoring: " + service.getName() + " is down",
            "Health check detected " + check.getLatencyMs() + " ms latency and "
                + check.getErrorRatePercent() + "% errors.",
            IncidentSeverity.CRITICAL
        );
        incident.assignService(service);
        Incident savedIncident = incidentRepository.save(incident);
        incidentEventService.record(
            savedIncident,
            IncidentEventType.INCIDENT_CREATED,
            "Incident created by monitoring for " + service.getName() + "."
        );

        Alert alert = alertRepository.save(new Alert(
            "Monitoring detected a service outage: " + service.getName() + ".",
            AlertSeverity.CRITICAL,
            savedIncident
        ));
        incidentEventService.record(
            savedIncident,
            IncidentEventType.ALERT_FIRED,
            "Alert fired: " + alert.getMessage()
        );
        return savedIncident;
    }

    private Incident findActiveIncident(UUID serviceId) {
        return incidentRepository.findFirstByServiceIdAndStatusInOrderByCreatedAtDesc(
            serviceId,
            List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED)
        ).orElse(null);
    }

    private ServiceHealthResponse toResponse(
        ServiceHealthCheck check,
        Incident activeIncident
    ) {
        return new ServiceHealthResponse(
            check.getService().getId(),
            check.getService().getName(),
            check.getStatus(),
            check.getLatencyMs(),
            check.getErrorRatePercent(),
            check.getCheckedAt(),
            activeIncident == null ? null : activeIncident.getId()
        );
    }
}
