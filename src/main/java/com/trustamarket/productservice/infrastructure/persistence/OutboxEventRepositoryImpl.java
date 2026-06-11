package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.application.port.OutboxEventRepository;
import com.trustamarket.productservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    public void save(String topic, String payload) {
        outboxEventJpaRepository.save(
                OutboxEventJpaEntity.builder()
                        .topic(topic)
                        .payload(payload)
                        .build()
        );
    }
}
