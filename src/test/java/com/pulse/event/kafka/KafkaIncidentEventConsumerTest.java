package com.pulse.event.kafka;

import com.pulse.event.entity.IncidentEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaIncidentEventConsumerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void shouldBroadcastConsumedEventToConnectedClients() {
        IncidentKafkaEvent event = new IncidentKafkaEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            IncidentEventType.INCIDENT_ACKNOWLEDGED,
            "Incident acknowledged.",
            Instant.parse("2026-08-30T12:00:00Z")
        );
        KafkaIncidentEventConsumer consumer = new KafkaIncidentEventConsumer(
            messagingTemplate
        );

        consumer.consume(event);

        verify(messagingTemplate).convertAndSend("/topic/incidents", event);
    }
}
