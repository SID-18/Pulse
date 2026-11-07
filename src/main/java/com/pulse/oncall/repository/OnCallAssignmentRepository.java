package com.pulse.oncall.repository;

import com.pulse.oncall.entity.OnCallAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OnCallAssignmentRepository extends JpaRepository<OnCallAssignment, UUID> {

    Optional<OnCallAssignment> findFirstByTeamIdAndStartsAtLessThanEqualAndEndsAtGreaterThanOrderByStartsAtDesc(
        UUID teamId,
        Instant startsAt,
        Instant endsAt
    );

    boolean existsByTeamIdAndStartsAtLessThanAndEndsAtGreaterThan(
        UUID teamId,
        Instant endsAt,
        Instant startsAt
    );
}
