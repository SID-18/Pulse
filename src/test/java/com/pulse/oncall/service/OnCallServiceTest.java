package com.pulse.oncall.service;

import com.pulse.oncall.dto.CreateOnCallAssignmentRequest;
import com.pulse.oncall.dto.OnCallAssignmentResponse;
import com.pulse.oncall.entity.OnCallAssignment;
import com.pulse.oncall.exception.InvalidOnCallAssignmentException;
import com.pulse.oncall.repository.OnCallAssignmentRepository;
import com.pulse.team.entity.Team;
import com.pulse.team.repository.TeamRepository;
import com.pulse.user.entity.User;
import com.pulse.user.entity.UserRole;
import com.pulse.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnCallServiceTest {

    @Mock private OnCallAssignmentRepository onCallAssignmentRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private OnCallService onCallService;

    @Test
    void shouldScheduleAnEligibleTeamMember() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Team team = new Team("Payments");
        ReflectionTestUtils.setField(team, "id", teamId);
        User user = new User("Aarav Sharma", "aarav@example.com", UserRole.ENGINEER, team);
        ReflectionTestUtils.setField(user, "id", userId);
        Instant start = Instant.now().plusSeconds(60);
        Instant end = start.plusSeconds(3600);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(onCallAssignmentRepository.existsByTeamIdAndStartsAtLessThanAndEndsAtGreaterThan(
            teamId, end, start)).thenReturn(false);
        when(onCallAssignmentRepository.save(any(OnCallAssignment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        OnCallAssignmentResponse response = onCallService.schedule(
            new CreateOnCallAssignmentRequest(teamId, userId, start, end)
        );

        assertEquals(teamId, response.teamId());
        assertEquals(userId, response.userId());
    }

    @Test
    void shouldRejectAnOverlappingTeamSchedule() {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Team team = new Team("Payments");
        ReflectionTestUtils.setField(team, "id", teamId);
        User user = new User("Aarav Sharma", "aarav@example.com", UserRole.ENGINEER, team);
        Instant start = Instant.now().plusSeconds(60);
        Instant end = start.plusSeconds(3600);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(onCallAssignmentRepository.existsByTeamIdAndStartsAtLessThanAndEndsAtGreaterThan(
            teamId, end, start)).thenReturn(true);

        assertThrows(InvalidOnCallAssignmentException.class, () -> onCallService.schedule(
            new CreateOnCallAssignmentRequest(teamId, userId, start, end)
        ));
    }
}
