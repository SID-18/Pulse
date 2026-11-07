package com.pulse.service.service;

import com.pulse.service.dto.CreateMonitoredServiceRequest;
import com.pulse.service.dto.MonitoredServiceResponse;
import com.pulse.service.entity.MonitoredService;
import com.pulse.service.exception.MonitoredServiceNameAlreadyExistsException;
import com.pulse.service.exception.MonitoredServiceNotFoundException;
import com.pulse.service.repository.MonitoredServiceRepository;
import com.pulse.team.entity.Team;
import com.pulse.team.exception.TeamNotFoundException;
import com.pulse.team.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final MonitoredServiceRepository monitoredServiceRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public MonitoredServiceResponse createService(
        CreateMonitoredServiceRequest request
    ) {
        monitoredServiceRepository.findByName(request.name())
            .ifPresent(service -> {
                throw new MonitoredServiceNameAlreadyExistsException(
                    request.name()
                );
            });

        MonitoredService service = new MonitoredService(
            request.name(),
            request.description()
        );

        return toResponse(monitoredServiceRepository.save(service));
    }

    public List<MonitoredServiceResponse> getServices() {
        return monitoredServiceRepository.findAll(
                Sort.by(Sort.Direction.ASC, "name")
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public MonitoredServiceResponse assignTeam(UUID serviceId, UUID teamId) {
        MonitoredService service = monitoredServiceRepository.findById(serviceId)
            .orElseThrow(() -> new MonitoredServiceNotFoundException(serviceId));
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new TeamNotFoundException(teamId));
        service.assignTeam(team);
        return toResponse(service);
    }

    private MonitoredServiceResponse toResponse(MonitoredService service) {
        Team team = service.getTeam();
        return new MonitoredServiceResponse(
            service.getId(), service.getName(), service.getDescription(), service.getCreatedAt(),
            team == null ? null : team.getId(), team == null ? null : team.getName()
        );
    }
}
