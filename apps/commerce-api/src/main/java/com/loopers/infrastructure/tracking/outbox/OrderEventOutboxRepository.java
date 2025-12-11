package com.loopers.infrastructure.tracking.outbox;

import com.loopers.domain.tracking.order.OrderEventOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEventOutboxRepository extends JpaRepository<OrderEventOutboxEntity, Long> {
}
