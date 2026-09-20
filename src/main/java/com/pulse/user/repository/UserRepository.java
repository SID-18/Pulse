package com.pulse.user.repository;

import com.pulse.user.entity.User;
import com.pulse.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    List<User> findByRoleInOrderByNameAsc(Collection<UserRole> roles);
}
