package com.pulse.bootstrap;

import com.pulse.team.entity.Team;
import com.pulse.team.repository.TeamRepository;
import com.pulse.user.entity.User;
import com.pulse.user.entity.UserRole;
import com.pulse.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("docker")
public class DockerBootstrapManager implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DockerBootstrapManager.class);

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final String name;
    private final String email;
    private final String password;
    private final String teamName;

    public DockerBootstrapManager(
        UserRepository userRepository,
        TeamRepository teamRepository,
        PasswordEncoder passwordEncoder,
        @Value("${pulse.bootstrap.admin.name}") String name,
        @Value("${pulse.bootstrap.admin.email}") String email,
        @Value("${pulse.bootstrap.admin.password}") String password,
        @Value("${pulse.bootstrap.admin.team-name}") String teamName
    ) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
        this.name = name;
        this.email = email;
        this.password = password;
        this.teamName = teamName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Docker bootstrap manager already exists: {}", email);
            return;
        }

        Team team = teamRepository.findByName(teamName)
            .orElseGet(() -> teamRepository.save(new Team(teamName)));

        userRepository.save(new User(
            name,
            email,
            UserRole.MANAGER,
            team,
            passwordEncoder.encode(password)
        ));

        log.info("Created Docker bootstrap manager: {}", email);
    }
}
