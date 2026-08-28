CREATE TABLE alerts (
    id UUID PRIMARY KEY,
    message TEXT NOT NULL,
    severity VARCHAR(10) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status VARCHAR(10) NOT NULL CHECK (status IN ('FIRING', 'RESOLVED')),
    incident_id UUID NOT NULL REFERENCES incidents(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_alerts_incident_id_created_at
    ON alerts (incident_id, created_at DESC);
