package com.loopers.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "event_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventOutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ref OrderEventType
     */
    private String eventType;
    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;  // "ORDER"
    private String aggregateId;    // orderId

    @Lob
    @Column(columnDefinition = "json")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private Instant occurredAt;
    private Instant sentAt;

    public static EventOutboxEntity ready(
            String eventType,
            AggregateType aggregateType,
            String aggregateId,
            String payload,
            Instant occurredAt
    ) {
        EventOutboxEntity e = new EventOutboxEntity();
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
