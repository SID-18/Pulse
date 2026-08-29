package com.pulse.auth.service;

import com.pulse.auth.dto.AuthResponse;
import com.pulse.auth.dto.LoginRequest;
import com.pulse.auth.dto.RegisterRequest;
import com.pulse.team.entity.Team;
import com.pulse.team.exception.TeamNotFoundException;
import com.pulse.team.repository.TeamRepository;
import com.pulse.user.entity.User;
import com.pulse.user.entity.UserRole;
import com.pulse.user.exception.UserEmailAlreadyExistsException;
import com.pulse.user.repository.UserRepository;
import com.pulse.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new UserEmailAlreadyExistsException(request.email());
        });
        Team team = teamRepository.findById(request.teamId())
            .orElseThrow(() -> new TeamNotFoundException(request.teamId()));
        User user = userRepository.save(new User(request.name(), request.email(),
            UserRole.ENGINEER, team, passwordEncoder.encode(request.password())));
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(this::invalidCredentials);
        if (user.getPasswordHash() == null
            || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return tokenFor(user);
    }

    private AuthResponse tokenFor(User user) {
        return new AuthResponse(jwtService.generateToken(user), "Bearer");
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
