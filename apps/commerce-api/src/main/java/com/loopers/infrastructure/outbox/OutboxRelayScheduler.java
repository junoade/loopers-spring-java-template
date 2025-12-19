package com.loopers.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {
    // kafka 모듈의 kafkaTemplate 반환타입과 맞출 것. 그렇지 않으면 빈 못 찾음
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final EventOutboxService outboxService;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishOutbox() {
        List<EventOutboxEntity> outboxRepositories =  outboxService.findReady(Instant.now(), PageRequest.of(0, 50));

        for(EventOutboxEntity event : outboxRepositories) {
            int claimed = outboxService.updateToPending(event.getId());
            if (claimed == 0) continue;

            publishToKafka(event);
        }

    }

    @Transactional
    public void publishToKafka(EventOutboxEntity event) {
        log.info("Publishing outbox event {}", event);
        String topic = event.getAggregateType().getTopic();
        kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload())
                // 비동기 콜백
                .whenComplete((r, ex) -> {
                    if(ex == null) {
                        outboxService.markSent(event.getId());
                    } else {
                        outboxService.markFailed(event.getId());
                    }
                });
    }

    // PENDING stuck 회수
    @Scheduled(fixedDelay = 10_000)
    public void recoverPending() {
        log.info("[OUTBOX] find recovery status from {} to {}", OutboxStatus.PENDING, OutboxStatus.READY);
        outboxService.recoverStuckPending();
    }

    // FAILED 재시도 복구
    @Scheduled(fixedDelay = 15_000)
    public void recoverFailed() {
        log.info("[OUTBOX] find recovery status from {} to {}", OutboxStatus.FAILED, OutboxStatus.READY);
        outboxService.recoverFailed();
    }

}

