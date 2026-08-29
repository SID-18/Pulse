package com.pulse.comment.controller;

import com.pulse.alert.repository.AlertRepository;
import com.pulse.comment.entity.IncidentComment;
import com.pulse.comment.repository.IncidentCommentRepository;
import com.pulse.event.repository.IncidentEventRepository;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.repository.IncidentRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class IncidentCommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentCommentRepository incidentCommentRepository;

    @Autowired
    private IncidentEventRepository incidentEventRepository;

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
        incidentEventRepository.deleteAll();
        incidentCommentRepository.deleteAll();
        incidentTaskRepository.deleteAll();
        alertRepository.deleteAll();
        incidentRepository.deleteAll();
        userRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    void shouldCreateCommentForIncidentThroughApi() throws Exception {
        Incident incident = saveIncident();
        User user = saveUser();
        String requestBody = """
            {
              "content": "Connection pool usage is within the expected range.",
              "authorId": "%s"
            }
            """.formatted(user.getId());

        mockMvc.perform(post("/api/incidents/{id}/comments", incident.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.incidentId").value(incident.getId().toString()))
            .andExpect(jsonPath("$.authorId").value(user.getId().toString()))
            .andExpect(jsonPath("$.authorName").value("Aarav Sharma"));
    }

    @Test
    void shouldListCommentsForIncidentInCreationOrder() throws Exception {
        Incident incident = saveIncident();
        User user = saveUser();
        incidentCommentRepository.save(new IncidentComment(
            "Investigating the database connection pool.", incident, user
        ));
        incidentCommentRepository.save(new IncidentComment(
            "Pool usage has returned to normal.", incident, user
        ));

        mockMvc.perform(get("/api/incidents/{id}/comments", incident.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].content")
                .value("Investigating the database connection pool."))
            .andExpect(jsonPath("$[1].content")
                .value("Pool usage has returned to normal."));
    }

    @Test
    void shouldReturnNotFoundForUnknownCommentAuthor() throws Exception {
        Incident incident = saveIncident();
        String requestBody = """
            {
              "content": "Investigating the database connection pool.",
              "authorId": "%s"
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/incidents/{id}/comments", incident.getId())
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
