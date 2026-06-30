package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query(value = """
        SELECT * FROM p_outbox_events
        WHERE published = false AND failed = false
        ORDER BY created_at
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<OutboxEventJpaEntity> lockNextBatch(@Param("batchSize") int batchSize);


    @Modifying
    @Query("DELETE FROM OutboxEventJpaEntity e WHERE e.published = true AND e.publishedAt < :cutoff")
    int deleteByPublishedTrueAndPublishedAtBefore(Instant cutoff);

    @Modifying
    @Query("DELETE FROM OutboxEventJpaEntity e WHERE e.failed = true AND e.lastFailedAt < :cutoff")
    int deleteByFailedTrueAndLastFailedAtBefore(Instant cutoff);
}
