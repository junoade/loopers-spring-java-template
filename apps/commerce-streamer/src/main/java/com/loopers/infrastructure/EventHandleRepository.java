package com.loopers.infrastructure;

import com.loopers.domain.EventHandledModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventHandleRepository extends JpaRepository<EventHandledModel, Long> {
    Optional<EventHandledModel> findByConsumerNameAndEventId(String consumerName, String eventId);
    long countByConsumerNameAndEventId(String consumerName, String eventId);
}
