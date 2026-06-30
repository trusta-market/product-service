-- Outbox 중복 처리 방지를 위한 claimed 컬럼 추가
ALTER TABLE p_outbox_events ADD COLUMN claimed boolean NOT NULL DEFAULT false;

-- claimed = false 조건 포함 부분 인덱스 (폴링 쿼리 성능)
CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON p_outbox_events (created_at)
    WHERE published = false AND failed = false AND claimed = false;