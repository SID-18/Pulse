package com.pulse.alert.controller;

import com.pulse.alert.dto.AlertResponse;
import com.pulse.alert.dto.CreateAlertRequest;
import com.pulse.alert.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse createAlert(
        @Valid @RequestBody CreateAlertRequest request
    ) {
        return alertService.createAlert(request);
    }

    @GetMapping
    public List<AlertResponse> getAlerts(
        @RequestParam(required = false) UUID incidentId
    ) {
        return alertService.getAlerts(incidentId);
    }

    @PatchMapping("/{id}/resolve")
    public AlertResponse resolveAlert(@PathVariable UUID id) {
        return alertService.resolveAlert(id);
    }
}
