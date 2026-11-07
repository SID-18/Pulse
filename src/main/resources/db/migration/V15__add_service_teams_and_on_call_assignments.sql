ALTER TABLE services
    ADD COLUMN team_id UUID
    REFERENCES teams(id);

CREATE INDEX idx_services_team_id ON services(team_id);

CREATE TABLE on_call_assignments (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES teams(id),
    user_id UUID NOT NULL REFERENCES users(id),
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_on_call_assignment_window CHECK (ends_at > starts_at)
);

CREATE INDEX idx_on_call_assignments_active
    ON on_call_assignments(team_id, starts_at, ends_at);
