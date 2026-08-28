package com.pulse.service.controller;

import com.pulse.alert.repository.AlertRepository;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.service.entity.MonitoredService;
import com.pulse.task.repository.IncidentTaskRepository;
import com.pulse.service.repository.MonitoredServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceCatalogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private IncidentTaskRepository incidentTaskRepository;

    @Autowired
    private MonitoredServiceRepository monitoredServiceRepository;

    @BeforeEach
    void clearTemporaryDatabase() {
        incidentTaskRepository.deleteAll();
        alertRepository.deleteAll();
        incidentRepository.deleteAll();
        monitoredServiceRepository.deleteAll();
    }

    @Test
    void shouldCreateServiceThroughApi() throws Exception {
        String requestBody = """
            {
              "name": "Payment API",
              "description": "Handles checkout payment processing."
            }
            """;

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Payment API"))
            .andExpect(jsonPath("$.description")
                .value("Handles checkout payment processing."));
    }

    @Test
    void shouldListServicesAlphabetically() throws Exception {
        monitoredServiceRepository.save(
            new MonitoredService("Search API", "Searches products.")
        );
        monitoredServiceRepository.save(
            new MonitoredService("Billing API", "Processes invoices.")
        );

        mockMvc.perform(get("/api/services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Billing API"))
            .andExpect(jsonPath("$[1].name").value("Search API"));
    }

    @Test
    void shouldReturnConflictForDuplicateServiceName() throws Exception {
        String requestBody = """
            {
              "name": "Payment API",
              "description": "Handles checkout payment processing."
            }
            """;
        mockMvc.perform(post("/api/services")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }
}
