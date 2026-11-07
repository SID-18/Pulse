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
            UUID.randomUUID(),
            UUID.randomUUID(),
            "ALERT_FIRED",
            "Monitoring detected a service outage.",
            Instant.parse("2026-09-06T10:00:00Z")
        );

        consumer.consume(event);

        assertEquals(1, notificationStore.getLatest(null, null).size());
        assertEquals("ALERT_FIRED", notificationStore.getLatest(null, null).getFirst().type());
    }

    @Test
    void shouldIgnoreADuplicateEventId() {
        NotificationStore notificationStore = new NotificationStore();
        IncidentNotificationConsumer consumer = new IncidentNotificationConsumer(
            notificationStore
        );
        NotificationEvent event = new NotificationEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "INCIDENT_CREATED",
            "Incident created.",
            Instant.parse("2026-09-13T09:00:00Z")
        );

        consumer.consume(event);
        consumer.consume(event);

        assertEquals(1, notificationStore.getLatest(null, null).size());
    }

    @Test
    void shouldFilterNotificationsForTheAssignedOwner() {
        NotificationStore notificationStore = new NotificationStore();
        IncidentNotificationConsumer consumer = new IncidentNotificationConsumer(notificationStore);
        UUID ownerId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        consumer.consume(new NotificationEvent(
            UUID.randomUUID(), UUID.randomUUID(), ownerId, teamId,
            "SLA_RESOLUTION_BREACHED", "SLA resolution target breached.", Instant.now()
        ));
        consumer.consume(new NotificationEvent(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            "SLA_ACKNOWLEDGEMENT_BREACHED", "SLA acknowledgement target breached.", Instant.now()
        ));

        assertEquals(1, notificationStore.getLatest(ownerId, null).size());
        assertEquals(1, notificationStore.getLatest(null, teamId).size());
    }
}
