package com.loopers.interfaces.api.tracking;

import com.loopers.domain.tracking.EventActorType;
import com.loopers.domain.tracking.UserActionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UserActionTrackingAspect {
    private final ApplicationEventPublisher eventPublisher;

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

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);   // 예: #productId = 10
        }

        Object value = parser.parseExpression(expression).getValue(context);

        return value != null ? value.toString() : null;
    }
}
