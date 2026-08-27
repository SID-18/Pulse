package com.pulse.incident.entity;

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
import com.pulse.incident.exception.InvalidIncidentStatusTransitionException;
import com.pulse.service.entity.MonitoredService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private MonitoredService service;

    public Incident(String title, String description, IncidentSeverity severity) {
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = IncidentStatus.OPEN;
    }

    @PrePersist
    void setCreatedAt() {
        createdAt = Instant.now();
    }

    public void acknowledge() {
        if (status != IncidentStatus.OPEN) {
            throw new InvalidIncidentStatusTransitionException(
                status,
                IncidentStatus.ACKNOWLEDGED
            );
        }

        status = IncidentStatus.ACKNOWLEDGED;
    }

    public void resolve() {
        if (status != IncidentStatus.ACKNOWLEDGED) {
            throw new InvalidIncidentStatusTransitionException(
                status,
                IncidentStatus.RESOLVED
            );
        }

        status = IncidentStatus.RESOLVED;
        resolvedAt = Instant.now();
    }

    public void assignService(MonitoredService service) {
        this.service = service;
    }
}
