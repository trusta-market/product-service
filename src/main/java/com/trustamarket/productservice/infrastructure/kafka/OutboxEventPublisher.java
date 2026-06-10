package com.trustamarket.productservice.infrastructure.kafka;

import com.trustamarket.productservice.infrastructure.persistence.OutboxEventJpaRepository;
import com.trustamarket.productservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventJpaRepository outboxEventRepository;
    private final OutboxEventProcessor outboxEventProcessor;

    @Value("${trusta.outbox.max-retry:5}")
    private int maxRetry;

    @Scheduled(fixedDelayString = "${trusta.outbox.poll-interval-ms:1000}")
    @SchedulerLock(name = "outbox_publisher", lockAtMostFor = "PT30S", lockAtLeastFor = "PT1S")
    public void publishPendingEvents() {
        List<OutboxEventJpaEntity> events =
                outboxEventRepository.findTop100ByPublishedFalseAndFailedFalseOrderByCreatedAtAsc();

        for (OutboxEventJpaEntity event : events) {
            boolean shouldContinue = outboxEventProcessor.process(event, maxRetry);
            if (!shouldContinue) {
                break;
            }
        }
    }
}
