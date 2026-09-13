package com.pulse.notification.kafka;

import com.pulse.notification.service.NotificationStore;
import org.springframework.kafka.annotation.KafkaListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IncidentNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(
        IncidentNotificationConsumer.class
    );

    private final NotificationStore notificationStore;

    public IncidentNotificationConsumer(NotificationStore notificationStore) {
        this.notificationStore = notificationStore;
    }

    @KafkaListener(topics = "${pulse.kafka.topics.incident-events}")
    public void consume(NotificationEvent event) {
        if (!notificationStore.record(event)) {
            log.info("Duplicate notification event ignored: eventId={}", event.eventId());
            return;
        }
        log.info(
            "Notification queued: incidentId={}, type={}, message={}",
            event.incidentId(),
            event.type(),
            event.message()
        );
    }
}
