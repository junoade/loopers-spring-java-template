package com.loopers.support.tracking.annotation;

import com.loopers.domain.order.event.OrderEventType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublishOrderEvent {
    OrderEventType value();
}
