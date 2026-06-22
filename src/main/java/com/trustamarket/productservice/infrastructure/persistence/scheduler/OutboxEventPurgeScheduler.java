package com.trustamarket.productservice.infrastructure.persistence.scheduler;

import com.trustamarket.productservice.infrastructure.persistence.OutboxEventJpaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPurgeScheduler {

    private static final int MIN_RETAIN_DAYS = 1;

    private final OutboxEventJpaRepository outboxEventRepository;

    @Value("${trusta.outbox.purge.published-retain-days:7}")
    private int publishedRetainDays;

    @Value("${trusta.outbox.purge.failed-retain-days:30}")
    private int failedRetainDays;

    @PostConstruct
    void validateRetainDays() {
        if (publishedRetainDays < MIN_RETAIN_DAYS) {
            throw new IllegalStateException(
                    "trusta.outbox.purge.published-retain-days 는 최소 %d 이상이어야 합니다. (현재: %d)"
                            .formatted(MIN_RETAIN_DAYS, publishedRetainDays));
        }
        if (failedRetainDays < MIN_RETAIN_DAYS) {
            throw new IllegalStateException(
                    "trusta.outbox.purge.failed-retain-days 는 최소 %d 이상이어야 합니다. (현재: %d)"
                            .formatted(MIN_RETAIN_DAYS, failedRetainDays));
        }
        log.info("[Outbox Purge] 설정 검증 완료 — published 보관: {}일, failed 보관: {}일",
                publishedRetainDays, failedRetainDays);
    }

    @Scheduled(cron = "${trusta.outbox.purge.cron:0 0 3 * * *}")
    @Transactional
    public void purge() {
        Instant publishedCutoff = Instant.now().minus(publishedRetainDays, ChronoUnit.DAYS);
        int deletedPublished = outboxEventRepository
                .deleteByPublishedTrueAndPublishedAtBefore(publishedCutoff);

        Instant failedCutoff = Instant.now().minus(failedRetainDays, ChronoUnit.DAYS);
        int deletedFailed = outboxEventRepository
                .deleteByFailedTrueAndLastFailedAtBefore(failedCutoff);

        log.info("[Outbox Purge] 완료 — published: {}건, failed: {}건 삭제",
                deletedPublished, deletedFailed);
    }
}