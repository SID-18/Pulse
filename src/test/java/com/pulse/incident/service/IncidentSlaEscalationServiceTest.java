package com.pulse.incident.service;

import com.pulse.alert.entity.AlertStatus;
import com.pulse.alert.repository.AlertRepository;
import com.pulse.event.entity.IncidentEvent;
import com.pulse.event.entity.IncidentEventType;
import com.pulse.event.repository.IncidentEventRepository;
import com.pulse.event.service.IncidentEventService;
import com.pulse.incident.dto.IncidentSlaResponse;
import com.pulse.incident.entity.Incident;
import com.pulse.incident.entity.IncidentSeverity;
import com.pulse.incident.entity.IncidentStatus;
import com.pulse.incident.repository.IncidentRepository;
import com.pulse.team.entity.Team;
import com.pulse.user.entity.User;
import com.pulse.user.entity.UserRole;
import com.pulse.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentSlaEscalationServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private IncidentEventRepository incidentEventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IncidentEventService incidentEventService;

    @Mock
    private IncidentSlaPolicy incidentSlaPolicy;

    @Test
    void shouldCreateAnAcknowledgementEscalationOnlyOnce() {
        Incident incident = incident(IncidentStatus.OPEN);
        when(incidentRepository.findAllByStatus(IncidentStatus.OPEN))
            .thenReturn(List.of(incident));
        when(incidentRepository.findAllByStatus(IncidentStatus.ACKNOWLEDGED))
            .thenReturn(List.of());
        when(incidentSlaPolicy.evaluate(incident)).thenReturn(new IncidentSlaResponse(
            Instant.now(), Instant.now().plusSeconds(3600), "BREACHED", "ON_TRACK"
        ));
        when(alertRepository.existsByIncidentIdAndMessageAndStatus(
            incident.getId(), IncidentSlaEscalationService.ACKNOWLEDGEMENT_BREACH_MESSAGE,
            AlertStatus.FIRING
        )).thenReturn(false);

        service().escalateBreachedIncidents();

        ArgumentCaptor<com.pulse.alert.entity.Alert> alert = ArgumentCaptor.forClass(
            com.pulse.alert.entity.Alert.class
        );
        verify(alertRepository).save(alert.capture());
        assertEquals(IncidentSlaEscalationService.ACKNOWLEDGEMENT_BREACH_MESSAGE,
            alert.getValue().getMessage());
        verify(incidentEventService).record(
            eq(incident), eq(IncidentEventType.SLA_ACKNOWLEDGEMENT_BREACHED),
            eq(IncidentSlaEscalationService.ACKNOWLEDGEMENT_BREACH_MESSAGE)
        );
    }

    @Test
    void shouldNotDuplicateAnExistingFiringResolutionEscalation() {
        Incident incident = incident(IncidentStatus.ACKNOWLEDGED);
        when(incidentRepository.findAllByStatus(IncidentStatus.OPEN)).thenReturn(List.of());
        when(incidentRepository.findAllByStatus(IncidentStatus.ACKNOWLEDGED))
            .thenReturn(List.of(incident));
        when(incidentSlaPolicy.evaluate(incident)).thenReturn(new IncidentSlaResponse(
            Instant.now(), Instant.now(), "ON_TRACK", "BREACHED"
        ));
        when(alertRepository.existsByIncidentIdAndMessageAndStatus(
            incident.getId(), IncidentSlaEscalationService.RESOLUTION_BREACH_MESSAGE,
            AlertStatus.FIRING
        )).thenReturn(true);

        service().escalateBreachedIncidents();

        verify(alertRepository, never()).save(any());
        verify(incidentEventService, never()).record(any(), any(), any());
    }

    @Test
    void shouldEscalateToTheTeamManagerAfterTheOwnerWindowExpires() {
        Team team = new Team("Payments");
        ReflectionTestUtils.setField(team, "id", UUID.randomUUID());
        User owner = user("On-call engineer", UserRole.ENGINEER, team);
        User manager = user("Payments manager", UserRole.MANAGER, team);
        Incident incident = incident(IncidentStatus.OPEN);
        incident.assignOwner(owner);
        IncidentEvent ownerNotification = org.mockito.Mockito.mock(IncidentEvent.class);

        when(incidentRepository.findAllByStatus(IncidentStatus.OPEN)).thenReturn(List.of(incident));
        when(incidentRepository.findAllByStatus(IncidentStatus.ACKNOWLEDGED)).thenReturn(List.of());
        when(incidentSlaPolicy.evaluate(incident)).thenReturn(new IncidentSlaResponse(
            Instant.now(), Instant.now().plusSeconds(3600), "BREACHED", "ON_TRACK"
        ));
        when(alertRepository.existsByIncidentIdAndMessageAndStatus(
            incident.getId(), IncidentSlaEscalationService.ACKNOWLEDGEMENT_BREACH_MESSAGE,
            AlertStatus.FIRING
        )).thenReturn(true);
        when(incidentEventRepository.existsByIncidentIdAndType(
            incident.getId(), IncidentEventType.SLA_MANAGER_ESCALATED
        )).thenReturn(false);
        when(incidentEventRepository.findFirstByIncidentIdAndTypeOrderByCreatedAtAsc(
            incident.getId(), IncidentEventType.SLA_ACKNOWLEDGEMENT_BREACHED
        )).thenReturn(Optional.of(ownerNotification));
        when(ownerNotification.getCreatedAt()).thenReturn(Instant.now().minus(Duration.ofMinutes(16)));
        when(userRepository.findFirstByTeamIdAndRoleOrderByNameAsc(team.getId(), UserRole.MANAGER))
            .thenReturn(Optional.of(manager));

        service().escalateBreachedIncidents();

        verify(incidentEventService).recordForRecipient(
            eq(incident), eq(IncidentEventType.SLA_MANAGER_ESCALATED),
            eq(IncidentSlaEscalationService.MANAGER_ESCALATION_MESSAGE), eq(manager)
        );
    }

    private IncidentSlaEscalationService service() {
        return new IncidentSlaEscalationService(
            incidentRepository, alertRepository, incidentEventRepository, userRepository,
            incidentEventService, incidentSlaPolicy, Duration.ofMinutes(15)
        );
    }

    private Incident incident(IncidentStatus status) {
        Incident incident = new Incident("Payment API failure", "Checkout failed.", IncidentSeverity.HIGH);
        ReflectionTestUtils.setField(incident, "id", UUID.randomUUID());
        if (status == IncidentStatus.ACKNOWLEDGED) {
            incident.acknowledge();
        }
        return incident;
    }

    private User user(String name, UserRole role, Team team) {
        User user = new User(name, name.replace(' ', '.') + "@pulse.local", role, team);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
