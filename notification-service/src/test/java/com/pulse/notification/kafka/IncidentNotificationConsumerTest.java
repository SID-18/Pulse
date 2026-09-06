package com.pulse.notification.kafka;

import com.pulse.notification.service.NotificationStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncidentNotificationConsumerTest {

    @Test
    void shouldStoreAConsumedIncidentEventAsANotification() {
        NotificationStore notificationStore = new NotificationStore();
        IncidentNotificationConsumer consumer = new IncidentNotificationConsumer(
            notificationStore
        );
        NotificationEvent event = new NotificationEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "ALERT_FIRED",
            "Monitoring detected a service outage.",
            Instant.parse("2026-09-06T10:00:00Z")
        );

        consumer.consume(event);

        assertEquals(1, notificationStore.getLatest().size());
        assertEquals("ALERT_FIRED", notificationStore.getLatest().getFirst().type());
    }
}
