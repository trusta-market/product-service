package com.trustamarket.productservice.infrastructure.kafka;

import com.trustamarket.productservice.infrastructure.persistence.OutboxEventJpaRepository;
import com.trustamarket.productservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxEventClaimer {

    private final OutboxEventJpaRepository outboxEventRepository;

    // 짧은 TX: SKIP LOCKED로 락 획득 → claimed = true 세팅 → 즉시 커밋
    // 커밋 후 락 해제되지만 claimed = false 조건 덕분에 다른 파드가 건너뜀
    @Transactional
    public List<OutboxEventJpaEntity> claimBatch(int batchSize) {
        List<OutboxEventJpaEntity> events = outboxEventRepository.lockNextBatch(batchSize);
        events.forEach(OutboxEventJpaEntity::markClaimed);
        outboxEventRepository.saveAll(events);
        return events;
    }
}