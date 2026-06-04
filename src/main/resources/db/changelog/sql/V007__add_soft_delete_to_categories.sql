ALTER TABLE p_product.p_categories
    ADD COLUMN IF NOT EXISTS deleted    BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_categories_not_deleted
    ON p_product.p_categories (id)
    WHERE deleted = FALSE;