package com.pulse.alert.entity;

import com.pulse.alert.exception.InvalidAlertStatusTransitionException;
import com.pulse.incident.entity.Incident;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlertStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public Alert(String message, AlertSeverity severity, Incident incident) {
        this.message = message;
        this.severity = severity;
        this.incident = incident;
        this.status = AlertStatus.FIRING;
    }

    @PrePersist
    void setCreatedAt() {
        createdAt = Instant.now();
    }

    public void resolve() {
        if (status != AlertStatus.FIRING) {
            throw new InvalidAlertStatusTransitionException(status, AlertStatus.RESOLVED);
        }

        status = AlertStatus.RESOLVED;
        resolvedAt = Instant.now();
    }
}
