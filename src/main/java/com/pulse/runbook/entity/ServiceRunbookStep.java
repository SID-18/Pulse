package com.pulse.runbook.entity;

import com.pulse.service.entity.MonitoredService;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "service_runbook_steps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceRunbookStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private MonitoredService service;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    public ServiceRunbookStep(
        MonitoredService service,
        int stepOrder,
        String title,
        String description
    ) {
        this.service = service;
        this.stepOrder = stepOrder;
        this.title = title;
        this.description = description;
    }
}
