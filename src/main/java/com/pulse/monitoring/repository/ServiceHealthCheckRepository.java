package com.pulse.monitoring.repository;

import com.pulse.monitoring.entity.ServiceHealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceHealthCheckRepository
    extends JpaRepository<ServiceHealthCheck, UUID> {

    Optional<ServiceHealthCheck> findFirstByServiceIdOrderByCheckedAtDesc(UUID serviceId);
}
