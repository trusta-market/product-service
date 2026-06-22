package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    List<OutboxEventJpaEntity> findByPublishedFalseAndFailedFalseOrderByCreatedAtAsc(Pageable pageable);

    @Modifying
    @Query("DELETE FROM OutboxEventJpaEntity e WHERE e.published = true AND e.publishedAt < :cutoff")
    int deleteByPublishedTrueAndPublishedAtBefore(Instant cutoff);

    @Modifying
    @Query("DELETE FROM OutboxEventJpaEntity e WHERE e.failed = true AND e.lastFailedAt < :cutoff")
    int deleteByFailedTrueAndLastFailedAtBefore(Instant cutoff);
}
