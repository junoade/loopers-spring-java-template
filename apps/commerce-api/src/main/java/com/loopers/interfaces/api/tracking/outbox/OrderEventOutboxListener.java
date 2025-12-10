package com.loopers.interfaces.api.tracking.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.tracking.order.OrderEventOutboxEntity;
import com.loopers.domain.tracking.order.OrderEventPayload;
import com.loopers.infrastructure.tracking.outbox.OrderEventOutboxRepository;
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
    private final OrderEventOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderEvent(OrderEventPayload payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            OrderEventOutboxEntity entity = OrderEventOutboxEntity.ready(
                    payload.eventType().name(),
                    "ORDER",
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
