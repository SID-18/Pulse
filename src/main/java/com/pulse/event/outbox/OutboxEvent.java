package com.pulse.event.outbox;

import com.pulse.event.entity.IncidentEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "event_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(nullable = false, length = 100)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private IncidentEventType eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public OutboxEvent(
        UUID id,
        UUID incidentId,
        String topic,
        IncidentEventType eventType,
        String message,
        Instant occurredAt
    ) {
        this.id = id;
        this.incidentId = incidentId;
        this.topic = topic;
        this.eventType = eventType;
        this.message = message;
        this.occurredAt = occurredAt;
        this.status = OutboxEventStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = Instant.now();
    }

    @PrePersist
    void setCreatedAt() {
        createdAt = Instant.now();
    }

    public void markPublished() {
        status = OutboxEventStatus.PUBLISHED;
        publishedAt = Instant.now();
        lastError = null;
    }

    public void markFailed(Throwable error) {
        attempts++;
        lastError = error.getMessage();
        long delaySeconds = Math.min(60, 1L << Math.min(attempts, 6));
        nextAttemptAt = Instant.now().plus(delaySeconds, ChronoUnit.SECONDS);
    }
}
