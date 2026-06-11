--liquibase formatted sql

--changeset product-service:9
-- ── [신규] V009__add_outbox_retry_fields.sql
-- 수정 이유: OutboxEventJpaEntity에 retryCount / lastFailedAt / failed 필드 추가에 대응.
--           failed=false 조건 인덱스를 추가해 스케줄러 쿼리 성능 보장.

ALTER TABLE p_outbox_events
    ADD COLUMN IF NOT EXISTS retry_count   INT         NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_failed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS failed        BOOLEAN     NOT NULL DEFAULT false;

-- 기존 인덱스(published=false 조건) 대체
-- failed=false 조건을 포함해 격리된 이벤트를 스캔 대상에서 제외
DROP INDEX IF EXISTS idx_outbox_unpublished;

CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON p_outbox_events (created_at)
    WHERE published = false AND failed = false;