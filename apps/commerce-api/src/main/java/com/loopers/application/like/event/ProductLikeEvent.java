package com.loopers.application.like.event;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.UUID;

public record ProductLikeEvent(
        String eventId,
        Long userPkId,
        Long productId,
        LikeEventType eventType,
        Instant occurredAt,
        String traceId
) {
    public static ProductLikeEvent from(Long userPkId, Long productId, LikeEventType eventType) {
        return new ProductLikeEvent(
                UUID.randomUUID().toString(),
                userPkId,
                productId,
                eventType,
                Instant.now(),
                MDC.get("traceId")
        );
    }
}
