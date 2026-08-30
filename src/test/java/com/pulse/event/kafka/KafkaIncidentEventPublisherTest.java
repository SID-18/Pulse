package com.pulse.event.kafka;

import com.pulse.event.domain.IncidentEventRecorded;
import com.pulse.event.entity.IncidentEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaIncidentEventPublisherTest {

    @Mock
    private KafkaTemplate<String, IncidentKafkaEvent> kafkaTemplate;

    @Test
    void shouldPublishAnAuditEventUsingTheIncidentIdAsKafkaKey() {
        UUID eventId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-08-30T12:00:00Z");
        IncidentEventRecorded event = new IncidentEventRecorded(
            eventId,
            incidentId,
            IncidentEventType.COMMENT_ADDED,
            "Comment added by Dev Engineer.",
            occurredAt
        );
        when(kafkaTemplate.send(anyString(), anyString(), any(IncidentKafkaEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        KafkaIncidentEventPublisher publisher = new KafkaIncidentEventPublisher(
            kafkaTemplate,
            "incident-events"
        );

        publisher.publish(event);

        ArgumentCaptor<IncidentKafkaEvent> eventCaptor = ArgumentCaptor.forClass(
            IncidentKafkaEvent.class
        );
        verify(kafkaTemplate).send(
            eq("incident-events"),
            eq(incidentId.toString()),
            eventCaptor.capture()
        );
        IncidentKafkaEvent publishedEvent = eventCaptor.getValue();
        assertEquals(eventId, publishedEvent.eventId());
        assertEquals(incidentId, publishedEvent.incidentId());
        assertEquals(IncidentEventType.COMMENT_ADDED, publishedEvent.type());
        assertEquals("Comment added by Dev Engineer.", publishedEvent.message());
        assertEquals(occurredAt, publishedEvent.occurredAt());
    }
}
