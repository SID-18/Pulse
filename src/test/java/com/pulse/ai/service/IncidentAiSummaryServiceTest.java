package com.pulse.ai.service;

import com.pulse.ai.client.OllamaClient;
import com.pulse.alert.repository.AlertRepository;
import com.pulse.comment.repository.IncidentCommentRepository;
import com.pulse.event.repository.IncidentEventRepository;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.task.repository.IncidentTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentAiSummaryServiceTest {

    @Mock private OllamaClient ollamaClient;
    @Mock private IncidentRepository incidentRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private IncidentTaskRepository taskRepository;
    @Mock private IncidentCommentRepository commentRepository;
    @Mock private IncidentEventRepository eventRepository;
    @InjectMocks private IncidentAiSummaryService incidentAiSummaryService;

    @Test
    void shouldGenerateALocalAiSummaryFromIncidentContext() {
        UUID incidentId = UUID.randomUUID();
        Incident incident = new Incident(
            "Payment API failure", "Checkout returns HTTP 500.",
            IncidentSeverity.CRITICAL
        );
        ReflectionTestUtils.setField(incident, "id", incidentId);
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        when(alertRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId))
            .thenReturn(List.of());
        when(taskRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId))
            .thenReturn(List.of());
        when(commentRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId))
            .thenReturn(List.of());
        when(eventRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId))
            .thenReturn(List.of());
        when(ollamaClient.generate(anyString())).thenReturn("Local summary");
        when(ollamaClient.model()).thenReturn("qwen2.5:3b");

        var response = incidentAiSummaryService.summarize(incidentId);

        assertEquals(incidentId, response.incidentId());
        assertEquals("qwen2.5:3b", response.model());
        assertEquals("Local summary", response.summary());
    }
}
