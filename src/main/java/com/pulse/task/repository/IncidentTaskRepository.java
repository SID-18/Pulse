package com.pulse.task.repository;

import com.pulse.task.entity.IncidentTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncidentTaskRepository
    extends JpaRepository<IncidentTask, UUID> {

    List<IncidentTask> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
