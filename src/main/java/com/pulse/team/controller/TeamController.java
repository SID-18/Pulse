package com.pulse.team.controller;

import com.pulse.team.dto.CreateTeamRequest;
import com.pulse.team.dto.TeamResponse;
import com.pulse.team.service.TeamService;
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
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(
        @Valid @RequestBody CreateTeamRequest request
    ) {
        return teamService.createTeam(request);
    }

    @GetMapping
    public List<TeamResponse> getTeams() {
        return teamService.getTeams();
    }
}
