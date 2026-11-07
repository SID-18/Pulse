package com.pulse.notification.kafka;

import com.pulse.notification.service.NotificationStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void shouldTrackReadStatePerRecipient() {
        NotificationStore notificationStore = new NotificationStore();
        UUID eventId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID teammateId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        new IncidentNotificationConsumer(notificationStore).consume(new NotificationEvent(
            eventId, UUID.randomUUID(), ownerId, teamId,
            "COMMENT_ADDED", "Comment added.", Instant.now()
        ));

        assertFalse(notificationStore.getLatest(ownerId, teamId).getFirst().read());
        assertTrue(notificationStore.markAsRead(eventId, ownerId, teamId));
        assertTrue(notificationStore.getLatest(ownerId, teamId).getFirst().read());
        assertFalse(notificationStore.getLatest(teammateId, teamId).getFirst().read());
    }
}
