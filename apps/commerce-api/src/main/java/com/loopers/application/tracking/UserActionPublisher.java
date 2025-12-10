package com.loopers.application.tracking;

import com.loopers.domain.tracking.UserActionEvent;

public interface UserActionPublisher {
    void publish(UserActionEvent event);
}
