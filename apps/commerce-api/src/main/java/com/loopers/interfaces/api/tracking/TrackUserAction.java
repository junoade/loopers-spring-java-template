package com.loopers.interfaces.api.tracking;

import com.loopers.domain.tracking.UserActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러/퍼사드에서 조회 이벤트를 AOP를 통해 처리하도록 합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackUserAction {
    UserActionType actionType();
    String entityType() default "";
    String entityId() default "";
}
