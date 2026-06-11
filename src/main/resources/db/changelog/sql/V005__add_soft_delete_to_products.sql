--liquibase formatted sql

--changeset Seungwon-Choi:9

ALTER TABLE p_product.p_products
    ADD COLUMN IF NOT EXISTS deleted    BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_products_not_deleted
    ON p_product.p_products (created_at DESC)
    WHERE deleted = FALSE;