package com.pulse.event.controller;

import com.pulse.alert.repository.AlertRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncidentEventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentEventRepository incidentEventRepository;

    @Autowired
    private IncidentCommentRepository incidentCommentRepository;

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
    void shouldRecordAndListCommentAddedEvent() throws Exception {
        Incident incident = saveIncident();
        User user = saveUser();
        String requestBody = """
            {
              "content": "Investigating the payment API latency spike.",
              "authorId": "%s"
            }
            """.formatted(user.getId());

        mockMvc.perform(post("/api/incidents/{id}/comments", incident.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/incidents/{id}/events", incident.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("COMMENT_ADDED"))
            .andExpect(jsonPath("$[0].message")
                .value("Comment added by Aarav Sharma."));
    }

    @Test
    void shouldRecordIncidentAcknowledgedEvent() throws Exception {
        Incident incident = saveIncident();

        mockMvc.perform(patch("/api/incidents/{id}/acknowledge", incident.getId()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/incidents/{id}/events", incident.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("INCIDENT_ACKNOWLEDGED"))
            .andExpect(jsonPath("$[0].message").value("Incident acknowledged."));
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
