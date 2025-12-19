package com.loopers.application.order.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.infrastructure.outbox.AggregateType;
import com.loopers.infrastructure.outbox.EventOutboxEntity;
import com.loopers.domain.order.event.OrderEventPayload;
import com.loopers.infrastructure.outbox.EventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventOutboxListener {
    private final EventOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * 주문, 결제 결과에 대한 데이터플랫폼 전송을 위한 후속처리
     * - [2025.12.12] outbox 테이블 내 insert
     * @param payload
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderEvent(OrderEventPayload payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            EventOutboxEntity entity = EventOutboxEntity.ready(
                    payload.eventType().name(),
                    AggregateType.ORDER,
                    payload.orderId().toString(),
                    json,
                    payload.occurredAt() != null ? payload.occurredAt() : Instant.now()
            );

            outboxRepository.save(entity);

            log.info("[OUTBOX] saved event. type={}, orderId={}, outboxId={}",
                    payload.eventType(), payload.orderId(), entity.getId());

        } catch (Exception e) {
            log.error("[OUTBOX] failed to save OrderEventPayload. payload={}", payload, e);
        }
    }
}
