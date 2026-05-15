ALTER TABLE p_products
    ADD COLUMN IF NOT EXISTS suggested_price BIGINT;