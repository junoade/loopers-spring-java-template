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

    @Column(nullable = false)
    private int retryCount;

    private Instant processingStartedAt;
    private Instant nextRetryAt;

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
        e.nextRetryAt = occurredAt;
        return e;
    }

    public void markPending() {
        this.status = OutboxStatus.PENDING;
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
        long backOffSeconds = calcBackoffSeconds(retryCount);
        this.nextRetryAt = Instant.now().plusSeconds(backOffSeconds);
    }

    private long calcBackoffSeconds(int retry) {
        return switch (retry) {
            case 1 -> 3;
            case 2 -> 10;
            case 3 -> 30;
            case 4 -> 60;
            default -> 120;
        };
    }
}
