CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'ENGINEER', 'VIEWER')),
    team_id UUID NOT NULL REFERENCES teams(id)
);

CREATE INDEX idx_users_team_id ON users (team_id);
