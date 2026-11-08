CREATE TABLE service_runbook_steps (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    step_order INTEGER NOT NULL CHECK (step_order > 0),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    CONSTRAINT uk_service_runbook_steps_service_order UNIQUE (service_id, step_order)
);

CREATE INDEX idx_service_runbook_steps_service_order
    ON service_runbook_steps(service_id, step_order);
