--liquibase formatted sql

--changeset Seungwon-Choi:12

ALTER TABLE p_outbox_events
    ADD COLUMN IF NOT EXISTS retry_count   INT         NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_failed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS failed        BOOLEAN     NOT NULL DEFAULT false;
