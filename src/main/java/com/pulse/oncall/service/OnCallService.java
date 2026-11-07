package com.pulse.oncall.service;

import com.pulse.oncall.dto.CreateOnCallAssignmentRequest;
import com.pulse.oncall.dto.OnCallAssignmentResponse;
import com.pulse.oncall.entity.OnCallAssignment;
import com.pulse.oncall.exception.InvalidOnCallAssignmentException;
import com.pulse.oncall.repository.OnCallAssignmentRepository;
import com.pulse.team.entity.Team;
import com.pulse.team.exception.TeamNotFoundException;
import com.pulse.team.repository.TeamRepository;
import com.pulse.user.entity.User;
import com.pulse.user.entity.UserRole;
import com.pulse.user.exception.UserNotFoundException;
import com.pulse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OnCallService {

    private final OnCallAssignmentRepository onCallAssignmentRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional
    public OnCallAssignmentResponse schedule(CreateOnCallAssignmentRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new InvalidOnCallAssignmentException("On-call end time must be after its start time.");
        }

        Team team = teamRepository.findById(request.teamId())
            .orElseThrow(() -> new TeamNotFoundException(request.teamId()));
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException(request.userId()));

        if (!user.getTeam().getId().equals(team.getId())) {
            throw new InvalidOnCallAssignmentException("The on-call user must belong to the selected team.");
        }
        if (user.getRole() != UserRole.MANAGER && user.getRole() != UserRole.ENGINEER) {
            throw new InvalidOnCallAssignmentException("Only managers and engineers can be on call.");
        }
        if (onCallAssignmentRepository.existsByTeamIdAndStartsAtLessThanAndEndsAtGreaterThan(
            team.getId(), request.endsAt(), request.startsAt())) {
            throw new InvalidOnCallAssignmentException("The selected team already has an overlapping on-call assignment.");
        }

        return toResponse(onCallAssignmentRepository.save(
            new OnCallAssignment(team, user, request.startsAt(), request.endsAt())
        ));
    }

    @Transactional(readOnly = true)
    public OnCallAssignmentResponse getActiveAssignment(UUID teamId, Instant now) {
        return onCallAssignmentRepository
            .findFirstByTeamIdAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByStartsAtDesc(teamId, now, now)
            .map(this::toResponse)
            .orElse(null);
    }

    private OnCallAssignmentResponse toResponse(OnCallAssignment assignment) {
        return new OnCallAssignmentResponse(
            assignment.getId(), assignment.getTeam().getId(), assignment.getTeam().getName(),
            assignment.getUser().getId(), assignment.getUser().getName(),
            assignment.getStartsAt(), assignment.getEndsAt()
        );
    }
}
