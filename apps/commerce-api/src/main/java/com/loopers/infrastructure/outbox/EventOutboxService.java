package com.loopers.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventOutboxService {
    private static final int MAX_RETRY = 5;
    private static final Duration PENDING_TIMEOUT = Duration.ofSeconds(30);

    private final EventOutboxRepository eventOutboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(Long outboxId) {
        log.info("Marking sent outbox {}", outboxId);
        eventOutboxRepository.findById(outboxId).ifPresent(EventOutboxEntity::markSent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long outboxId) {
        log.info("Marking failed outbox {}", outboxId);
        eventOutboxRepository.findById(outboxId).ifPresent(EventOutboxEntity::markFailed);
    }

    @Transactional(readOnly = true)
    public List<EventOutboxEntity> findReady(Instant instant, Pageable pageable) {
        return eventOutboxRepository.findReady(Instant.now(), PageRequest.of(0, 50));
    }

    @Transactional
    public int updateToPending(Long outboxId) {
        return eventOutboxRepository.updateToPending(outboxId, Instant.now());
    }


    /**
     * 처리중(PENDING) 으로 남은 것을 READY 상태로 복원합니다.
     * - PENDING_TIMEOUT을 기준으로 복원합니다.
     * @return
     */
    @Transactional
    public int recoverStuckPending() {
        Instant now = Instant.now();
        Instant expiredAt = now.minus(PENDING_TIMEOUT);
        int recovered = eventOutboxRepository.recoverStuckPending(expiredAt, now);

        if (recovered > 0) {
            log.warn("[OUTBOX] recovered {} stuck PENDING events (expiredAt={})", recovered, expiredAt);
        }
        return recovered;
    }

    /**
     * pub 단계에서 실패한 경우에 대해 복원합니다.
     * - MAX_RETRY 만큼 백오프 를 두어 시도합니다.
     * @return
     */
    @Transactional
    public int recoverFailed() {
        Instant now = Instant.now();
        int recovered = eventOutboxRepository.recoverFailed(now, MAX_RETRY);

        if (recovered > 0) {
            log.info("[OUTBOX] recovered {} FAILED events (now={})", recovered, now);
        }
        return recovered;
    }
}
