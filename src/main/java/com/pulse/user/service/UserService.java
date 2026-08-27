package com.pulse.user.service;

import com.pulse.team.entity.Team;
import com.pulse.team.exception.TeamNotFoundException;
import com.pulse.team.repository.TeamRepository;
import com.pulse.user.dto.CreateUserRequest;
import com.pulse.user.dto.UserResponse;
import com.pulse.user.entity.User;
import com.pulse.user.exception.UserEmailAlreadyExistsException;
import com.pulse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        userRepository.findByEmail(request.email())
            .ifPresent(user -> {
                throw new UserEmailAlreadyExistsException(request.email());
            });

        Team team = teamRepository.findById(request.teamId())
            .orElseThrow(() -> new TeamNotFoundException(request.teamId()));

        User user = new User(
            request.name(),
            request.email(),
            request.role(),
            team
        );

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private UserResponse toResponse(User user) {
        Team team = user.getTeam();

        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            team.getId(),
            team.getName()
        );
    }
}
