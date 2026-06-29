-- 판매자별 활성 상품 (deleted=false인 행만 인덱싱, 정렬 순서 포함)
CREATE INDEX IF NOT EXISTS idx_products_seller_active_recent
    ON p_products (seller_id, created_at DESC)
    WHERE deleted = false;

-- 카테고리별 판매중 상품
CREATE INDEX IF NOT EXISTS idx_products_cat_status_active_recent
    ON p_products (category_id, status, created_at DESC)
    WHERE deleted = false;

-- Outbox 정리용
CREATE INDEX IF NOT EXISTS idx_outbox_published_at
    ON p_outbox_events (published_at)
    WHERE published = true;

CREATE INDEX IF NOT EXISTS idx_outbox_failed_last_failed_at
    ON p_outbox_events (last_failed_at)
    WHERE failed = true;

-- 부분 인덱스로 대체된 단일 컬럼 인덱스 제거
DROP INDEX IF EXISTS idx_products_seller_id;
DROP INDEX IF EXISTS idx_products_status;
-- idx_products_category_id 는 FK 조인용으로 유지