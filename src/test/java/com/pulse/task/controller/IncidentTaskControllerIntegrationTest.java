package com.pulse.task.controller;

import com.pulse.alert.repository.AlertRepository;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.task.entity.IncidentTask;
import com.pulse.task.repository.IncidentTaskRepository;
import com.pulse.team.entity.Team;
import com.pulse.team.repository.TeamRepository;
import com.pulse.user.entity.User;
import com.pulse.user.entity.UserRole;
import com.pulse.user.repository.UserRepository;
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
class IncidentTaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentTaskRepository incidentTaskRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @BeforeEach
    void clearTemporaryDatabase() {
        incidentTaskRepository.deleteAll();
        alertRepository.deleteAll();
        incidentRepository.deleteAll();
        userRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    void shouldCreateTaskForIncidentThroughApi() throws Exception {
        Incident incident = saveIncident();
        User user = saveUser();
        String requestBody = """
            {
              "title": "Check database connection pool usage",
              "description": "Inspect active and idle connections.",
              "assignedUserId": "%s"
            }
            """.formatted(user.getId());

        mockMvc.perform(post("/api/incidents/{id}/tasks", incident.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("TODO"))
            .andExpect(jsonPath("$.incidentId").value(incident.getId().toString()))
            .andExpect(jsonPath("$.assignedUserId").value(user.getId().toString()));
    }

    @Test
    void shouldListTasksForIncident() throws Exception {
        Incident incident = saveIncident();
        incidentTaskRepository.save(new IncidentTask(
            "Check logs", "Review application logs.", incident, null
        ));

        mockMvc.perform(get("/api/incidents/{id}/tasks", incident.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Check logs"));
    }

    @Test
    void shouldStartAndCompleteTask() throws Exception {
        Incident incident = saveIncident();
        IncidentTask task = incidentTaskRepository.save(new IncidentTask(
            "Check logs", "Review application logs.", incident, null
        ));

        mockMvc.perform(patch("/api/tasks/{id}/start", task.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/tasks/{id}/complete", task.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DONE"))
            .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundForUnknownAssignedUser() throws Exception {
        Incident incident = saveIncident();
        String requestBody = """
            {
              "title": "Check logs",
              "assignedUserId": "%s"
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/incidents/{id}/tasks", incident.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    private Incident saveIncident() {
        return incidentRepository.save(new Incident(
            "Payment API failure",
            "Checkout requests return HTTP 500.",
            IncidentSeverity.CRITICAL
        ));
    }

    private User saveUser() {
        Team team = teamRepository.save(new Team("Payments"));

        return userRepository.save(new User(
            "Aarav Sharma",
            "aarav@example.com",
            UserRole.ENGINEER,
            team
        ));
    }
}
