package com.trustamarket.productservice.infrastructure.persistence.scheduler;

import com.trustamarket.productservice.infrastructure.persistence.OutboxEventJpaRepository;
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

    private final OutboxEventJpaRepository outboxEventRepository;

    @Value("${trusta.outbox.purge.published-retain-days:7}")
    private int publishedRetainDays;

    @Value("${trusta.outbox.purge.failed-retain-days:30}")
    private int failedRetainDays;

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
