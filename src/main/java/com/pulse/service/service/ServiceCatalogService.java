package com.pulse.service.service;

import com.pulse.service.dto.CreateMonitoredServiceRequest;
import com.pulse.service.dto.MonitoredServiceResponse;
import com.pulse.service.entity.MonitoredService;
import com.pulse.service.exception.MonitoredServiceNameAlreadyExistsException;
import com.pulse.service.repository.MonitoredServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final MonitoredServiceRepository monitoredServiceRepository;

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

    private MonitoredServiceResponse toResponse(MonitoredService service) {
        return new MonitoredServiceResponse(
            service.getId(),
            service.getName(),
            service.getDescription(),
            service.getCreatedAt()
        );
    }
}
