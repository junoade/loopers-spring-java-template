package com.loopers.domain.tracking.general;

import java.lang.annotation.*;

/**
 * 컨트롤러/퍼사드에서 조회 이벤트를 AOP를 통해 처리하도록 합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TrackUserAction {
    UserActionType actionType();
    String entityType() default "";
    String entityId() default "";
}
