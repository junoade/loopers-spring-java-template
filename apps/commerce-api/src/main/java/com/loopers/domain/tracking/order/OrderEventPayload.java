package com.loopers.domain.tracking.order;

import com.loopers.domain.order.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderEventPayload(
   OrderEventType eventType,
   Long orderId,
   String userId,
   Long totalPrice,
   OrderStatus status,
   List<Long> productIds,
   Instant occurredAt,
   Long couponId
) { }
