package com.loopers.application.idempotency;

import com.loopers.domain.EventHandledModel;
import com.loopers.infrastructure.EventHandleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventHandledService {
    private final EventHandleRepository eventHandleRepository;

    @Transactional
    public boolean tryHandle(String consumerName, String eventId) {
        if(!eventHandleRepository.findByConsumerNameAndEventId(consumerName, eventId).isEmpty()) {
            return false;
        }
        eventHandleRepository.save(EventHandledModel.of(consumerName, eventId));
        return true;
    }
}
