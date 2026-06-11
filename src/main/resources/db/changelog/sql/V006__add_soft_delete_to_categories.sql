--liquibase formatted sql

--changeset Seungwon-Choi:10

ALTER TABLE p_product.p_categories
    ADD COLUMN IF NOT EXISTS deleted    BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
