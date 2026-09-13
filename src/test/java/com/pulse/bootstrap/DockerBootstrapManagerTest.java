package com.pulse.bootstrap;

import com.pulse.team.entity.Team;
import com.pulse.team.repository.TeamRepository;
import com.pulse.user.entity.User;
import com.pulse.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DockerBootstrapManagerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final TeamRepository teamRepository = mock(TeamRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void createsManagerAndTeamWhenNeitherExists() {
        DockerBootstrapManager manager = manager();
        when(userRepository.findByEmail("admin@pulse.local")).thenReturn(Optional.empty());
        when(teamRepository.findByName("Platform Operations")).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("PulseDockerPass!2026")).thenReturn("encoded-password");

        manager.run(new DefaultApplicationArguments());

        verify(teamRepository).save(any(Team.class));
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("PulseDockerPass!2026");
    }

    @Test
    void leavesExistingManagerUnchanged() {
        DockerBootstrapManager manager = manager();
        when(userRepository.findByEmail("admin@pulse.local")).thenReturn(Optional.of(mock(User.class)));

        manager.run(new DefaultApplicationArguments());

        verify(teamRepository, never()).save(any(Team.class));
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    private DockerBootstrapManager manager() {
        return new DockerBootstrapManager(
            userRepository,
            teamRepository,
            passwordEncoder,
            "Pulse Docker Admin",
            "admin@pulse.local",
            "PulseDockerPass!2026",
            "Platform Operations"
        );
    }
}
