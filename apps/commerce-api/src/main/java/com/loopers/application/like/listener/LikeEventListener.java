package com.loopers.application.like.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.like.event.ProductLikeEvent;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.like.event.LikeEventType;
import com.loopers.infrastructure.outbox.AggregateType;
import com.loopers.infrastructure.outbox.EventOutboxEntity;
import com.loopers.infrastructure.outbox.EventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;


@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventListener {
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final EventOutboxRepository outboxRepository;

    /**
     * UserLikeProductFacade 서비스에서 발행한 상품좋아요/상품 싫어요 이벤트를 처리합니다.
     * @param event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductLikeEvent(ProductLikeEvent event) {
        log.debug("Like event received: {}", event);

        if(!isValidEventType(event.eventType())) {
            return;
        }

        handleEvent(event);
    }

    private boolean isValidEventType(LikeEventType eventType) {
        return eventType == LikeEventType.LIKE_CREATED || eventType == LikeEventType.LIKE_DELETED;
    }

    private void handleEvent(ProductLikeEvent event) {
        if(event.eventType() == LikeEventType.LIKE_CREATED) {
            productService.increaseProductLikeCount(event.productId());
        } else if(event.eventType() == LikeEventType.LIKE_DELETED) {
            productService.decreaseProductLikeCount(event.productId());
        } else {
            log.info("[info] - Duplicated Like event received: {}", event);
            return;
        }

        outboxCreated(event);
    }

    private void outboxCreated(ProductLikeEvent payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            EventOutboxEntity entity = EventOutboxEntity.ready(
                    payload.eventType().name(),
                    AggregateType.PRODUCT_LIKE,
                    payload.productId().toString(),
                    json,
                    payload.occurredAt() != null ? payload.occurredAt() : Instant.now()
            );

            outboxRepository.save(entity);

            log.info("[OUTBOX] saved event. type={}, orderId={}, outboxId={}",
                    payload.eventType(), payload.productId(), entity.getId());

        } catch (Exception e) {
            log.error("[OUTBOX] failed to save ProductLikeEvent. payload={}", payload, e);
        }
    }
}
