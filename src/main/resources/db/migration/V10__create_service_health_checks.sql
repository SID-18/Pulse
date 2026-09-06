CREATE TABLE service_health_checks (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL REFERENCES services(id),
    status VARCHAR(10) NOT NULL,
    latency_ms INTEGER NOT NULL,
    error_rate_percent DECIMAL(5,2) NOT NULL,
    checked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_health_checks_service_checked_at
    ON service_health_checks (service_id, checked_at DESC);
