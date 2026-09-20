package com.pulse.alert.repository;

import com.pulse.alert.entity.Alert;
import com.pulse.alert.entity.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId);

    boolean existsByIncidentIdAndMessageAndStatus(
        UUID incidentId,
        String message,
        AlertStatus status
    );
}
