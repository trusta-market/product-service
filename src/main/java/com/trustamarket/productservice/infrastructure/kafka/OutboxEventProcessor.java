package com.trustamarket.productservice.infrastructure.kafka;

import com.trustamarket.productservice.infrastructure.persistence.OutboxEventJpaRepository;
import com.trustamarket.productservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final OutboxEventJpaRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 이벤트 하나마다 독립 트랜잭션 — 성공한 건 즉시 커밋, 실패해도 다른 이벤트에 영향 없음
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean process(OutboxEventJpaEntity event, int maxRetry) {
        try {
            kafkaTemplate.send(event.getTopic(), event.getPayload()).get();
            event.markPublished();
            outboxEventRepository.save(event);
            log.info("[Outbox] 발행 완료 - topic: {}, id: {}", event.getTopic(), event.getId());
            return true;
        } catch (Exception e) {
            event.incrementRetryCount();
            if (event.getRetryCount() >= maxRetry) {
                event.markFailed();
                outboxEventRepository.save(event);
                log.error("[Outbox] 최대 재시도({}) 초과, 격리 처리 — topic: {}, id: {}. 수동 확인 필요.",
                        maxRetry, event.getTopic(), event.getId(), e);
                return true; // failed=true로 격리했으므로 다음 이벤트 계속 처리
            }
            outboxEventRepository.save(event);
            log.error("[Outbox] 발행 실패 - topic: {}, id: {}", event.getTopic(), event.getId(), e);
            return false; // 순서 보장을 위해 이번 배치 중단
        }
    }
}
