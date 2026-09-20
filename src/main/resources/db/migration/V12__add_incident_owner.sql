ALTER TABLE incidents ADD COLUMN owner_user_id UUID
    REFERENCES users(id);

CREATE INDEX idx_incidents_owner_user_id
    ON incidents(owner_user_id);
