package com.pulse.incident.controller;

import com.pulse.alert.repository.AlertRepository;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.service.entity.MonitoredService;
import com.pulse.service.repository.MonitoredServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private MonitoredServiceRepository monitoredServiceRepository;

    @BeforeEach
    void clearTemporaryDatabase() {
        alertRepository.deleteAll();
        incidentRepository.deleteAll();
        monitoredServiceRepository.deleteAll();
    }

    @Test
    void shouldCreateIncidentThroughApi() throws Exception {
        String requestBody = """
            {
              "title": "Payment API failure",
              "description": "Checkout requests return HTTP 500.",
              "severity": "CRITICAL"
            }
            """;

        mockMvc.perform(post("/api/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Payment API failure"))
            .andExpect(jsonPath("$.severity").value("CRITICAL"))
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.serviceId").doesNotExist());
    }

    @Test
    void shouldReturnValidationDetailsForBlankIncidentTitle() throws Exception {
        String requestBody = """
            {
              "title": " ",
              "description": "Checkout requests return HTTP 500.",
              "severity": "CRITICAL"
            }
            """;

        mockMvc.perform(post("/api/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Request validation failed"))
            .andExpect(jsonPath("$.errors.title").value("must not be blank"));
    }

    @Test
    void shouldReturnNotFoundForUnknownIncident() throws Exception {
        mockMvc.perform(get("/api/incidents/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnRequestedIncidentPage() throws Exception {
        incidentRepository.save(new Incident(
            "Checkout failure",
            "Checkout requests return HTTP 500.",
            IncidentSeverity.CRITICAL
        ));
        incidentRepository.save(new Incident(
            "Email delay",
            "Notifications are delayed.",
            IncidentSeverity.HIGH
        ));
        incidentRepository.save(new Incident(
            "Search latency",
            "Search results take too long.",
            IncidentSeverity.MEDIUM
        ));

        mockMvc.perform(get("/api/incidents")
                .param("page", "0")
                .param("size", "2")
                .param("sortBy", "TITLE")
                .param("direction", "ASC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("Checkout failure"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void shouldRejectInvalidPageSize() throws Exception {
        mockMvc.perform(get("/api/incidents").param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail")
                .value("One or more request parameters are invalid"));
    }

    @Test
    void shouldReturnConflictWhenResolvingOpenIncident() throws Exception {
        Incident incident = incidentRepository.save(new Incident(
            "Payment API failure",
            "Checkout requests return HTTP 500.",
            IncidentSeverity.CRITICAL
        ));

        mockMvc.perform(patch("/api/incidents/{id}/resolve", incident.getId()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldAssignServiceThroughApi() throws Exception {
        Incident incident = incidentRepository.save(new Incident(
            "Payment API failure",
            "Checkout requests return HTTP 500.",
            IncidentSeverity.CRITICAL
        ));
        MonitoredService service = monitoredServiceRepository.save(
            new MonitoredService(
                "Payment API",
                "Handles checkout payment processing."
            )
        );

        mockMvc.perform(patch(
                "/api/incidents/{incidentId}/service/{serviceId}",
                incident.getId(),
                service.getId()
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.serviceId").value(service.getId().toString()));
    }
}
