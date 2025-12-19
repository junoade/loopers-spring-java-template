package com.loopers.infrastructure.outbox;

import com.loopers.domain.order.event.OrderEventType;
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
public class OutboxRelayScheduler {
    // kafka 모듈의 kafkaTemplate 반환타입과 맞출 것. 그렇지 않으면 빈 못 찾음
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final EventOutboxRepository outboxRepository;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishOutbox() {
        List<EventOutboxEntity> outboxRepositories = outboxRepository.findByStatus(OutboxStatus.READY);

        for(EventOutboxEntity event : outboxRepositories) {
            publishToKafka(event);
            event.markSent();
        }

    }

    private void publishToKafka(EventOutboxEntity event) {
        log.info("Publishing outbox event {}", event);
        String topic = event.getAggregateType().getTopic();
        kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());
    }

}

