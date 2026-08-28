package com.pulse.task.entity;

import com.pulse.incident.entity.Incident;
import com.pulse.task.exception.InvalidIncidentTaskStatusTransitionException;
import com.pulse.user.entity.User;
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
@Table(name = "incident_tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncidentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentTaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public IncidentTask(
        String title,
        String description,
        Incident incident,
        User assignedUser
    ) {
        this.title = title;
        this.description = description;
        this.incident = incident;
        this.assignedUser = assignedUser;
        this.status = IncidentTaskStatus.TODO;
    }

    @PrePersist
    void setCreatedAt() {
        createdAt = Instant.now();
    }

    public void start() {
        transitionTo(IncidentTaskStatus.IN_PROGRESS);
    }

    public void complete() {
        transitionTo(IncidentTaskStatus.DONE);
        completedAt = Instant.now();
    }

    private void transitionTo(IncidentTaskStatus targetStatus) {
        boolean validTransition = (status == IncidentTaskStatus.TODO
            && targetStatus == IncidentTaskStatus.IN_PROGRESS)
            || (status == IncidentTaskStatus.IN_PROGRESS
            && targetStatus == IncidentTaskStatus.DONE);

        if (!validTransition) {
            throw new InvalidIncidentTaskStatusTransitionException(
                status,
                targetStatus
            );
        }

        status = targetStatus;
    }
}
