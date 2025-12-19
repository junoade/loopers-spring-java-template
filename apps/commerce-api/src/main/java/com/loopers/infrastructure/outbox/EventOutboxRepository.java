package com.loopers.infrastructure.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EventOutboxRepository extends JpaRepository<EventOutboxEntity, Long> {
    List<EventOutboxEntity> findByStatus(OutboxStatus status);

    @Query("""
    select e from EventOutboxEntity e
     where e.status = 'READY'
       and e.nextRetryAt <= :now
     order by e.occurredAt
    """)
    List<EventOutboxEntity> findReady(@Param("now") Instant now, Pageable pageable);

    // 선점 (READY → PENDING)
    @Modifying
    @Query("""
    update EventOutboxEntity e
       set e.status = 'PENDING',
           e.processingStartedAt = :now
     where e.id = :id
       and e.status = 'READY'
    """)
    int updateToPending(@Param("id") Long id, @Param("now") Instant now);

    @Modifying
    @Query("""
    update EventOutboxEntity e
       set e.status = 'READY',
           e.processingStartedAt = null,
           e.nextRetryAt = :now
     where e.status = 'PROCESSING'
       and e.processingStartedAt < :stuckBefore
    """)
    int recoverStuckPending(@Param("stuckBefore") Instant stuckBefore,
                   @Param("now") Instant now);

    @Modifying
    @Query("""
        update EventOutboxEntity e
           set e.status = 'READY'
         where e.status = 'FAILED'
           and e.nextRetryAt <= :now
           and e.retryCount < :maxRetry
    """)
    int recoverFailed(@Param("now") Instant now,
                      @Param("maxRetry") int maxRetry);
}
