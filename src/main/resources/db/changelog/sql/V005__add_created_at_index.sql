--liquibase formatted sql

--changeset trusta:8
-- /api/products/latest 의 ORDER BY created_at DESC LIMIT 10 가 Seq Scan.
-- 현재 row 9개라 무관하지만 row 증가 시 효과 큼. DESC index 로 backward scan 회피.
CREATE INDEX IF NOT EXISTS idx_products_created_at ON p_products (created_at DESC);
