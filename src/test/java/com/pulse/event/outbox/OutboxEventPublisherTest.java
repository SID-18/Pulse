package com.pulse.event.outbox;

import com.pulse.event.entity.IncidentEventType;
import com.pulse.event.kafka.IncidentKafkaEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, IncidentKafkaEvent> kafkaTemplate;

    @Test
    void publishesPendingEventUsingIncidentIdAsKafkaKey() {
        OutboxEvent event = event();
        when(outboxEventRepository.lockPublishableEvents(
            eq(OutboxEventStatus.PENDING), any(), any(Pageable.class)
        )).thenReturn(List.of(event));
        when(kafkaTemplate.send(anyString(), anyString(), any(IncidentKafkaEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        publisher().publishPendingEvents();

        ArgumentCaptor<IncidentKafkaEvent> eventCaptor = ArgumentCaptor.forClass(
            IncidentKafkaEvent.class
        );
        verify(kafkaTemplate).send(
            eq("incident-events"),
            eq(event.getIncidentId().toString()),
            eventCaptor.capture()
        );
        assertEquals(event.getId(), eventCaptor.getValue().eventId());
        assertEquals(OutboxEventStatus.PUBLISHED, event.getStatus());
    }

    @Test
    void retainsFailedEventForRetry() {
        OutboxEvent event = event();
        when(outboxEventRepository.lockPublishableEvents(
            eq(OutboxEventStatus.PENDING), any(), any(Pageable.class)
        )).thenReturn(List.of(event));
        when(kafkaTemplate.send(anyString(), anyString(), any(IncidentKafkaEvent.class)))
            .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Kafka offline")));

        publisher().publishPendingEvents();

        assertEquals(OutboxEventStatus.PENDING, event.getStatus());
        assertEquals(1, event.getAttempts());
        assertTrue(event.getLastError().contains("Kafka offline"));
    }

    private OutboxEventPublisher publisher() {
        return new OutboxEventPublisher(outboxEventRepository, kafkaTemplate, 25);
    }

    private OutboxEvent event() {
        return new OutboxEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "incident-events",
            IncidentEventType.COMMENT_ADDED,
            "Comment added by Dev Engineer.",
            Instant.parse("2026-09-13T08:00:00Z")
        );
    }
}
