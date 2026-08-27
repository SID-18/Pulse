package com.pulse.service.repository;

import com.pulse.service.entity.MonitoredService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MonitoredServiceRepository
    extends JpaRepository<MonitoredService, UUID> {

    Optional<MonitoredService> findByName(String name);
}
