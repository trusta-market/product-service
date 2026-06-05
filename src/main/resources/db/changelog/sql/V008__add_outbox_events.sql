--liquibase formatted sql

--changeset product-service:8
CREATE TABLE IF NOT EXISTS p_outbox_events (
                                               id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    topic        VARCHAR(100) NOT NULL,
    payload      TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    published    BOOLEAN     NOT NULL DEFAULT false,
    published_at TIMESTAMPTZ,
    PRIMARY KEY (id)
    );

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished
    ON p_outbox_events (created_at)
    WHERE published = false;