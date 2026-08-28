package com.pulse.alert.controller;

import com.pulse.alert.entity.Alert;
import com.pulse.alert.entity.AlertSeverity;
import com.pulse.alert.repository.AlertRepository;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.repository.IncidentRepository;
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
class AlertControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @BeforeEach
    void clearTemporaryDatabase() {
        alertRepository.deleteAll();
        incidentRepository.deleteAll();
    }

    @Test
    void shouldCreateAlertThroughApi() throws Exception {
        Incident incident = incidentRepository.save(new Incident(
            "Payment API failure",
            "Checkout requests return HTTP 500.",
            IncidentSeverity.CRITICAL
        ));
        String requestBody = """
            {
              "message": "Payment API error rate exceeded 5%%.",
              "severity": "CRITICAL",
              "incidentId": "%s"
            }
            """.formatted(incident.getId());

        mockMvc.perform(post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("FIRING"))
            .andExpect(jsonPath("$.incidentId").value(incident.getId().toString()));
    }

    @Test
    void shouldFilterAlertsByIncident() throws Exception {
        Incident paymentIncident = incidentRepository.save(new Incident(
            "Payment API failure", "Checkout requests fail.", IncidentSeverity.CRITICAL
        ));
        Incident searchIncident = incidentRepository.save(new Incident(
            "Search latency", "Search requests are slow.", IncidentSeverity.HIGH
        ));
        alertRepository.save(new Alert(
            "Payment API error rate exceeded 5%.",
            AlertSeverity.CRITICAL,
            paymentIncident
        ));
        alertRepository.save(new Alert(
            "Search latency exceeded threshold.",
            AlertSeverity.HIGH,
            searchIncident
        ));

        mockMvc.perform(get("/api/alerts")
                .param("incidentId", paymentIncident.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].incidentId")
                .value(paymentIncident.getId().toString()))
            .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void shouldResolveFiringAlert() throws Exception {
        Incident incident = incidentRepository.save(new Incident(
            "Payment API failure", "Checkout requests fail.", IncidentSeverity.CRITICAL
        ));
        Alert alert = alertRepository.save(new Alert(
            "Payment API error rate exceeded 5%.",
            AlertSeverity.CRITICAL,
            incident
        ));

        mockMvc.perform(patch("/api/alerts/{id}/resolve", alert.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RESOLVED"))
            .andExpect(jsonPath("$.resolvedAt").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundForUnknownIncident() throws Exception {
        String requestBody = """
            {
              "message": "Payment API error rate exceeded 5%%.",
              "severity": "CRITICAL",
              "incidentId": "%s"
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
}
