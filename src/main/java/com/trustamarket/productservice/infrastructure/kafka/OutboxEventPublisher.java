package com.trustamarket.productservice.infrastructure.kafka;

import com.trustamarket.productservice.infrastructure.persistence.OutboxEventJpaRepository;
import com.trustamarket.productservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventJpaRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventJpaEntity> events =
                outboxEventRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEventJpaEntity event : events) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getPayload()).get();
                event.markPublished();
                log.info("[Outbox] 발행 완료 - topic: {}, id: {}", event.getTopic(), event.getId());
            } catch (Exception e) {
                log.error("[Outbox] 발행 실패 - topic: {}, id: {}", event.getTopic(), event.getId(), e);
                break; // 순서 보장을 위해 실패 시 이번 배치 중단, 다음 폴링에서 재시도
            }
        }
    }
}
