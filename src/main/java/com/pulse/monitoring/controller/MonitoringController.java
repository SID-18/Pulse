package com.pulse.monitoring.controller;

import com.pulse.monitoring.dto.CreateHealthCheckRequest;
import com.pulse.monitoring.dto.ServiceHealthResponse;
import com.pulse.monitoring.service.MonitoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitoring/services")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping
    public List<ServiceHealthResponse> getServiceHealth() {
        return monitoringService.getServiceHealth();
    }

    @PostMapping("/{serviceId}/checks")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceHealthResponse recordHealthCheck(
        @PathVariable UUID serviceId,
        @Valid @RequestBody CreateHealthCheckRequest request
    ) {
        return monitoringService.recordHealthCheck(serviceId, request);
    }
}
