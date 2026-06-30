package com.trustamarket.productservice.infrastructure.kafka;

import com.trustamarket.productservice.infrastructure.persistence.OutboxEventJpaRepository;
import com.trustamarket.productservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProducer {

    private final OutboxEventJpaRepository outboxEventRepository;
    private final OutboxEventProcessor outboxEventProcessor;

    @Value("${trusta.outbox.max-retry:5}")
    private int maxRetry;

    @Value("${trusta.outbox.batch-size:500}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${trusta.outbox.poll-interval-ms:1000}")
    @Transactional
    public void producePendingEvents() {
        List<OutboxEventJpaEntity> events = outboxEventRepository.lockNextBatch(batchSize);

        for (OutboxEventJpaEntity event : events) {
            boolean shouldContinue = outboxEventProcessor.process(event, maxRetry);
            if (!shouldContinue) {
                break;
            }
        }
    }
}