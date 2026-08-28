CREATE TABLE incident_tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    incident_id UUID NOT NULL REFERENCES incidents(id),
    assigned_to UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_incident_tasks_incident_id_created_at
    ON incident_tasks (incident_id, created_at ASC);

CREATE INDEX idx_incident_tasks_assigned_to
    ON incident_tasks (assigned_to);
