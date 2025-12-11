package com.loopers.domain.tracking.order;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublishOrderEvent {
    OrderEventType value();
}
