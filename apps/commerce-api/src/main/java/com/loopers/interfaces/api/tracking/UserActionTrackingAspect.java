package com.loopers.interfaces.api.tracking;

import com.loopers.domain.tracking.general.EventActorType;
import com.loopers.domain.tracking.general.TrackUserAction;
import com.loopers.domain.tracking.general.UserActionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UserActionTrackingAspect {
    private final ApplicationEventPublisher eventPublisher;

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Around("@annotation(track)")
    public Object track(ProceedingJoinPoint joinPoint, TrackUserAction track) throws Throwable {
        Object result = joinPoint.proceed();

        // 메서드 파라미터에서 entityId 추출 (SpEL)
        String entityId = evalSpEL(track.entityId(), joinPoint);
        UserActionEvent event = new UserActionEvent(
                UUID.randomUUID().toString(),
                EventActorType.USER,
                track.actionType(),
                track.entityType(),
                entityId,
                "",
                "",
                MDC.get("traceId"),
                Instant.now(),
                Map.of()
        );

        log.info("UserActionTrackingAspect.track({})", event);
        eventPublisher.publishEvent(event);
        return result;
    }

    private String evalSpEL(String expression, ProceedingJoinPoint joinPoint) {
        if (expression == null || expression.isBlank()) {
            return null;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Spring이 알아서 파라미터 이름/값을 컨텍스트에 넣어줌
        EvaluationContext context = new MethodBasedEvaluationContext(
                null,                // root object (필요 없으면 null)
                method,
                joinPoint.getArgs(),
                NAME_DISCOVERER
        );

        Object value = PARSER.parseExpression(expression).getValue(context);
        return value != null ? value.toString() : null;
    }
}
