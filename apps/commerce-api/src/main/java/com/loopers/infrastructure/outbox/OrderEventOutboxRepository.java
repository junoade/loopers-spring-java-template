package com.loopers.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderEventOutboxRepository extends JpaRepository<OrderEventOutboxEntity, Long> {
    List<OrderEventOutboxEntity> findByStatus(OutboxStatus status);
}
