package com.loopers.application.tracking;

import com.loopers.domain.tracking.UserActionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActionPublisherV1 implements UserActionPublisher {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(UserActionEvent event) {
        eventPublisher.publishEvent(event);
    }
}
