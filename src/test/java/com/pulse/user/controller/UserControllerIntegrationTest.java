package com.pulse.user.controller;

import com.pulse.alert.repository.AlertRepository;
import com.pulse.comment.repository.IncidentCommentRepository;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.service.repository.MonitoredServiceRepository;
import com.pulse.team.entity.Team;
import com.pulse.team.repository.TeamRepository;
import com.pulse.user.entity.User;
import com.pulse.user.entity.UserRole;
import com.pulse.user.repository.UserRepository;
import com.pulse.task.repository.IncidentTaskRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

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
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @BeforeEach
    void clearTemporaryDatabase() {
        incidentCommentRepository.deleteAll();
        incidentTaskRepository.deleteAll();
        alertRepository.deleteAll();
        incidentRepository.deleteAll();
        monitoredServiceRepository.deleteAll();
        userRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    void shouldCreateUserThroughApi() throws Exception {
        Team team = teamRepository.save(new Team("Payments"));
        String requestBody = """
            {
              "name": "Aarav Sharma",
              "email": "aarav@example.com",
              "role": "ENGINEER",
              "teamId": "%s"
            }
            """.formatted(team.getId());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Aarav Sharma"))
            .andExpect(jsonPath("$.email").value("aarav@example.com"))
            .andExpect(jsonPath("$.role").value("ENGINEER"))
            .andExpect(jsonPath("$.teamId").value(team.getId().toString()))
            .andExpect(jsonPath("$.teamName").value("Payments"));
    }

    @Test
    void shouldListUsersAlphabetically() throws Exception {
        Team team = teamRepository.save(new Team("Payments"));
        userRepository.save(new User(
            "Zoya Khan",
            "zoya@example.com",
            UserRole.VIEWER,
            team
        ));
        userRepository.save(new User(
            "Aarav Sharma",
            "aarav@example.com",
            UserRole.ENGINEER,
            team
        ));

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Aarav Sharma"))
            .andExpect(jsonPath("$[1].name").value("Zoya Khan"));
    }

    @Test
    void shouldReturnConflictForDuplicateUserEmail() throws Exception {
        Team team = teamRepository.save(new Team("Payments"));
        String requestBody = """
            {
              "name": "Aarav Sharma",
              "email": "aarav@example.com",
              "role": "ENGINEER",
              "teamId": "%s"
            }
            """.formatted(team.getId());

        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturnNotFoundForUnknownTeam() throws Exception {
        String requestBody = """
            {
              "name": "Aarav Sharma",
              "email": "aarav@example.com",
              "role": "ENGINEER",
              "teamId": "%s"
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
}
