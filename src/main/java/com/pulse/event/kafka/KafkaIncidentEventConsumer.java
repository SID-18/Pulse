package com.pulse.event.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@Slf4j
public class KafkaIncidentEventConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    public KafkaIncidentEventConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "${pulse.kafka.topics.incident-events}")
    public void consume(IncidentKafkaEvent event) {
        log.info(
            "Kafka incident event received: eventId={}, incidentId={}, type={}",
            event.eventId(),
            event.incidentId(),
            event.type()
        );
        messagingTemplate.convertAndSend("/topic/incidents", event);
    }
}
