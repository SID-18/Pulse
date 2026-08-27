package com.pulse.team.service;

import com.pulse.team.dto.CreateTeamRequest;
import com.pulse.team.dto.TeamResponse;
import com.pulse.team.entity.Team;
import com.pulse.team.exception.TeamNameAlreadyExistsException;
import com.pulse.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request) {
        teamRepository.findByName(request.name())
            .ifPresent(team -> {
                throw new TeamNameAlreadyExistsException(request.name());
            });

        Team team = new Team(request.name());

        return toResponse(teamRepository.save(team));
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeams() {
        return teamRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private TeamResponse toResponse(Team team) {
        return new TeamResponse(team.getId(), team.getName());
    }
}
