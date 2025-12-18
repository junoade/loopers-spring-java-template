package com.loopers.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "order_event_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEventOutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ref OrderEventType
     */
    private String eventType;
    private String aggregateType;  // "ORDER"
    private String aggregateId;    // orderId

    @Lob
    @Column(columnDefinition = "json")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private Instant occurredAt;
    private Instant sentAt;

    public static OrderEventOutboxEntity ready(
            String eventType,
            String aggregateType,
            String aggregateId,
            String payload,
            Instant occurredAt
    ) {
        OrderEventOutboxEntity e = new OrderEventOutboxEntity();
        e.eventType = eventType;
        e.aggregateType = aggregateType;
        e.aggregateId = aggregateId;
        e.payload = payload;
        e.status = OutboxStatus.READY;
        e.occurredAt = occurredAt;
        return e;
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }
}
