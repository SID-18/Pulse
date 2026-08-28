CREATE TABLE incident_comments (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    incident_id UUID NOT NULL REFERENCES incidents(id),
    author_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_incident_comments_incident_id_created_at
    ON incident_comments (incident_id, created_at ASC);

CREATE INDEX idx_incident_comments_author_id
    ON incident_comments (author_id);
