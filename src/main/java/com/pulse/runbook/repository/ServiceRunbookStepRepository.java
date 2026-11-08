package com.pulse.runbook.repository;

import com.pulse.runbook.entity.ServiceRunbookStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceRunbookStepRepository extends JpaRepository<ServiceRunbookStep, UUID> {

    List<ServiceRunbookStep> findByServiceIdOrderByStepOrderAsc(UUID serviceId);

    boolean existsByServiceIdAndStepOrder(UUID serviceId, int stepOrder);
}
