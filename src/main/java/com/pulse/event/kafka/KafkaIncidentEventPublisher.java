package com.pulse.event.kafka;

import com.pulse.event.domain.IncidentEventRecorded;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Profile("!test")
@Slf4j
public class KafkaIncidentEventPublisher {

    private final KafkaTemplate<String, IncidentKafkaEvent> kafkaTemplate;
    private final String incidentEventsTopic;

    public KafkaIncidentEventPublisher(
        KafkaTemplate<String, IncidentKafkaEvent> kafkaTemplate,
        @Value("${pulse.kafka.topics.incident-events}") String incidentEventsTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.incidentEventsTopic = incidentEventsTopic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(IncidentEventRecorded event) {
        IncidentKafkaEvent kafkaEvent = new IncidentKafkaEvent(
            event.eventId(),
            event.incidentId(),
            event.type(),
            event.message(),
            event.occurredAt()
        );

        kafkaTemplate.send(
            incidentEventsTopic,
            event.incidentId().toString(),
            kafkaEvent
        ).whenComplete((result, error) -> {
            if (error != null) {
                log.error("Failed to publish Kafka incident event {}", event.eventId(), error);
            }
        });
    }
}
