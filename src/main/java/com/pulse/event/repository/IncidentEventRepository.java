package com.pulse.event.repository;

import com.pulse.event.entity.IncidentEvent;
import com.pulse.event.entity.IncidentEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentEventRepository extends JpaRepository<IncidentEvent, UUID> {

    List<IncidentEvent> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);

    boolean existsByIncidentIdAndType(UUID incidentId, IncidentEventType type);

    Optional<IncidentEvent> findFirstByIncidentIdAndTypeOrderByCreatedAtAsc(
        UUID incidentId,
        IncidentEventType type
    );
}
