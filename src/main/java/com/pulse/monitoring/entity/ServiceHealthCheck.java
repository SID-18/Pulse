package com.pulse.monitoring.entity;

import com.pulse.service.entity.MonitoredService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_health_checks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceHealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private MonitoredService service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private HealthStatus status;

    @Column(name = "latency_ms", nullable = false)
    private int latencyMs;

    @Column(name = "error_rate_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal errorRatePercent;

    @Column(name = "checked_at", nullable = false, updatable = false)
    private Instant checkedAt;

    public ServiceHealthCheck(
        MonitoredService service,
        HealthStatus status,
        int latencyMs,
        BigDecimal errorRatePercent
    ) {
        this.service = service;
        this.status = status;
        this.latencyMs = latencyMs;
        this.errorRatePercent = errorRatePercent;
    }

    @PrePersist
    void setCheckedAt() {
        checkedAt = Instant.now();
    }
}
