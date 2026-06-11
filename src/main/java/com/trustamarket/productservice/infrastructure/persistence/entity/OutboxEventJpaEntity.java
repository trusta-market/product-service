package com.trustamarket.productservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.time.Instant;

@Entity
@Table(name = "p_outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID outboxId;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean published = false;

    @Column
    private Instant publishedAt;

    @Column(nullable = false)
    private int retryCount = 0;         // 발행 시도 실패 횟수

    @Column
    private Instant lastFailedAt;       // 마지막 실패 시각 (모니터링용)

    @Column(nullable = false)
    private boolean failed = false;

    @Builder
    public OutboxEventJpaEntity(String topic, String payload) {
        this.topic = topic;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public void markPublished() {
        this.published = true;
        this.publishedAt = Instant.now();
    }

    // 실패 횟수 증가
    public void incrementRetryCount() {
        this.retryCount++;
        this.lastFailedAt = Instant.now();
    }

    //  최대 재시도 초과 시 격리
    public void markFailed() {
        this.failed = true;
    }
}
