package com.pulse.service.controller;

import com.pulse.service.dto.CreateMonitoredServiceRequest;
import com.pulse.service.dto.MonitoredServiceResponse;
import com.pulse.service.service.ServiceCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MonitoredServiceResponse createService(
        @Valid @RequestBody CreateMonitoredServiceRequest request
    ) {
        return serviceCatalogService.createService(request);
    }

    @GetMapping
    public List<MonitoredServiceResponse> getServices() {
        return serviceCatalogService.getServices();
    }
}
