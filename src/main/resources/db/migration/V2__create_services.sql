CREATE TABLE services (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE incidents
    ADD COLUMN service_id UUID
    REFERENCES services(id);

CREATE INDEX idx_incidents_service_id
    ON incidents (service_id);
