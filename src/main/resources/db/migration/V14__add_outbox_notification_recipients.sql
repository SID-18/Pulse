ALTER TABLE event_outbox
    ADD COLUMN recipient_user_id UUID;

ALTER TABLE event_outbox
    ADD COLUMN recipient_team_id UUID;
