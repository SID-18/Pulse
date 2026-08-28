package com.pulse.team.controller;

import com.pulse.alert.repository.AlertRepository;
import com.pulse.comment.repository.IncidentCommentRepository;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.service.repository.MonitoredServiceRepository;
import com.pulse.team.entity.Team;
import com.pulse.task.repository.IncidentTaskRepository;
import com.pulse.team.repository.TeamRepository;
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
class TeamControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private IncidentTaskRepository incidentTaskRepository;

    @Autowired
    private IncidentCommentRepository incidentCommentRepository;

    @Autowired
    private MonitoredServiceRepository monitoredServiceRepository;

    @Autowired
    private TeamRepository teamRepository;

    @BeforeEach
    void clearTemporaryDatabase() {
        incidentCommentRepository.deleteAll();
        incidentTaskRepository.deleteAll();
        alertRepository.deleteAll();
        incidentRepository.deleteAll();
        monitoredServiceRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    void shouldCreateTeamThroughApi() throws Exception {
        String requestBody = """
            {
              "name": "Payments"
            }
            """;

        mockMvc.perform(post("/api/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Payments"));
    }

    @Test
    void shouldListTeamsAlphabetically() throws Exception {
        teamRepository.save(new Team("Search"));
        teamRepository.save(new Team("Payments"));

        mockMvc.perform(get("/api/teams"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Payments"))
            .andExpect(jsonPath("$[1].name").value("Search"));
    }

    @Test
    void shouldReturnConflictForDuplicateTeamName() throws Exception {
        String requestBody = """
            {
              "name": "Payments"
            }
            """;

        mockMvc.perform(post("/api/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        mockMvc.perform(post("/api/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }
}
