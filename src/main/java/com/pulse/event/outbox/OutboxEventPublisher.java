package com.pulse.event.outbox;

import com.pulse.event.kafka.IncidentKafkaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@Profile("!test")
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, IncidentKafkaEvent> kafkaTemplate;
    private final int batchSize;

    public OutboxEventPublisher(
        OutboxEventRepository outboxEventRepository,
        KafkaTemplate<String, IncidentKafkaEvent> kafkaTemplate,
        @Value("${pulse.outbox.publisher.batch-size:25}") int batchSize
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${pulse.outbox.publisher.fixed-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.lockPublishableEvents(
            OutboxEventStatus.PENDING,
            Instant.now(),
            PageRequest.of(0, batchSize)
        );

        for (OutboxEvent event : events) {
            publish(event);
        }
    }

    private void publish(OutboxEvent event) {
        try {
            kafkaTemplate.send(
                event.getTopic(),
                event.getIncidentId().toString(),
                new IncidentKafkaEvent(
                    event.getId(),
                    event.getIncidentId(),
                    event.getEventType(),
                    event.getMessage(),
                    event.getOccurredAt()
                )
            ).get();
            event.markPublished();
            log.info("Published outbox event {} to Kafka", event.getId());
        } catch (Exception error) {
            event.markFailed(error);
            log.warn(
                "Kafka publish failed for outbox event {}; retry {} is scheduled for {}",
                event.getId(),
                event.getAttempts(),
                event.getNextAttemptAt(),
                error
            );
        }
    }
}
