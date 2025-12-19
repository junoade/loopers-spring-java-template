package com.loopers.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "event_handled",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_handled_consumer_event",
                        columnNames = {"consumer_name", "event_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventHandledModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_name", nullable = false, length = 100)
    private String consumerName;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "handled_at", nullable = false)
    private Instant handledAt;

    public static EventHandledModel of(String consumerName, String eventId) {
        EventHandledModel e = new EventHandledModel();
        e.consumerName = consumerName;
        e.eventId = eventId;
        e.handledAt = Instant.now();
        return e;
    }
}
