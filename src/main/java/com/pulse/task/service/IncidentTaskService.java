package com.pulse.task.service;

import com.pulse.incident.entity.Incident;
import com.pulse.incident.exception.IncidentNotFoundException;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.task.dto.CreateIncidentTaskRequest;
import com.pulse.task.dto.IncidentTaskResponse;
import com.pulse.task.entity.IncidentTask;
import com.pulse.task.exception.IncidentTaskNotFoundException;
import com.pulse.task.repository.IncidentTaskRepository;
import com.pulse.user.entity.User;
import com.pulse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentTaskService {

    private final IncidentTaskRepository incidentTaskRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    @Transactional
    public IncidentTaskResponse createTask(
        UUID incidentId,
        CreateIncidentTaskRequest request
    ) {
        Incident incident = findIncidentById(incidentId);
        User assignedUser = request.assignedUserId() == null
            ? null
            : userRepository.findById(request.assignedUserId())
                .orElseThrow(() -> new com.pulse.user.exception.UserNotFoundException(
                    request.assignedUserId()
                ));

        IncidentTask task = new IncidentTask(
            request.title(),
            request.description(),
            incident,
            assignedUser
        );

        return toResponse(incidentTaskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<IncidentTaskResponse> getTasks(UUID incidentId) {
        findIncidentById(incidentId);

        return incidentTaskRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public IncidentTaskResponse startTask(UUID id) {
        IncidentTask task = findTaskById(id);
        task.start();

        return toResponse(task);
    }

    @Transactional
    public IncidentTaskResponse completeTask(UUID id) {
        IncidentTask task = findTaskById(id);
        task.complete();

        return toResponse(task);
    }

    private Incident findIncidentById(UUID id) {
        return incidentRepository.findById(id)
            .orElseThrow(() -> new IncidentNotFoundException(id));
    }

    private IncidentTask findTaskById(UUID id) {
        return incidentTaskRepository.findById(id)
            .orElseThrow(() -> new IncidentTaskNotFoundException(id));
    }

    private IncidentTaskResponse toResponse(IncidentTask task) {
        User assignedUser = task.getAssignedUser();

        return new IncidentTaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getIncident().getId(),
            assignedUser == null ? null : assignedUser.getId(),
            assignedUser == null ? null : assignedUser.getName(),
            task.getCreatedAt(),
            task.getCompletedAt()
        );
    }
}
