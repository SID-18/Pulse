package com.pulse.ai.service;

import com.pulse.ai.client.OllamaClient;
import com.pulse.ai.dto.IncidentAiSummaryResponse;
import com.pulse.alert.repository.AlertRepository;
import com.pulse.comment.repository.IncidentCommentRepository;
import com.pulse.event.repository.IncidentEventRepository;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.exception.IncidentNotFoundException;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.task.repository.IncidentTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class IncidentAiSummaryService {

    private final OllamaClient ollamaClient;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final IncidentTaskRepository taskRepository;
    private final IncidentCommentRepository commentRepository;
    private final IncidentEventRepository eventRepository;

    public IncidentAiSummaryService(
        OllamaClient ollamaClient,
        IncidentRepository incidentRepository,
        AlertRepository alertRepository,
        IncidentTaskRepository taskRepository,
        IncidentCommentRepository commentRepository,
        IncidentEventRepository eventRepository
    ) {
        this.ollamaClient = ollamaClient;
        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public IncidentAiSummaryResponse summarize(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new IncidentNotFoundException(incidentId));
        String summary = ollamaClient.generate(buildPrompt(incident));

        return new IncidentAiSummaryResponse(
            incident.getId(),
            ollamaClient.model(),
            summary,
            Instant.now()
        );
    }

    private String buildPrompt(Incident incident) {
        String alerts = alertRepository.findByIncidentIdOrderByCreatedAtDesc(
                incident.getId()
            )
            .stream()
            .map(alert -> "- " + alert.getSeverity() + " " + alert.getStatus()
                + ": " + alert.getMessage())
            .reduce("", (left, right) -> left + "\n" + right);
        String tasks = taskRepository.findByIncidentIdOrderByCreatedAtAsc(incident.getId())
            .stream()
            .map(task -> "- " + task.getStatus() + ": " + task.getTitle())
            .reduce("", (left, right) -> left + "\n" + right);
        String comments = commentRepository
            .findByIncidentIdOrderByCreatedAtAsc(incident.getId())
            .stream()
            .map(comment -> "- " + comment.getContent())
            .reduce("", (left, right) -> left + "\n" + right);
        String events = eventRepository.findByIncidentIdOrderByCreatedAtAsc(incident.getId())
            .stream()
            .map(event -> "- " + event.getType() + ": " + event.getMessage())
            .reduce("", (left, right) -> left + "\n" + right);

        return """
            You are a local incident-management assistant. Use only the supplied facts.
            Do not invent root causes, actions, or outcomes. Give a concise response with
            these headings: Summary, Current impact, Recommended next steps. Limit next
            steps to three practical, conditional suggestions.

            Incident:
            Title: %s
            Description: %s
            Severity: %s
            Status: %s
            Service: %s

            Alerts:%s

            Tasks:%s

            Comments:%s

            Timeline:%s
            """.formatted(
            incident.getTitle(),
            incident.getDescription(),
            incident.getSeverity(),
            incident.getStatus(),
            incident.getService() == null ? "Unassigned" : incident.getService().getName(),
            alerts.isBlank() ? " None" : alerts,
            tasks.isBlank() ? " None" : tasks,
            comments.isBlank() ? " None" : comments,
            events.isBlank() ? " None" : events
        );
    }
}
