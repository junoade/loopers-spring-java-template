package com.loopers.interfaces.api.tracking;

import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderResult;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.tracking.order.OrderEventPayload;
import com.loopers.domain.tracking.order.OrderEventType;
import com.loopers.domain.tracking.order.PublishOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OrderEventPublishingAspect {
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 주문 결제에 대한 이벤트를 AOP로 처리합니다.
     * - 비즈니스 로직 실행 후
     * - 특정 응답 형식일 때 이벤트를 발생시킵니다.
     * @param joinPoint
     * @param publishOrderEvent
     * @return
     * @throws Throwable
     */
    @Around("@annotation(publishOrderEvent)")
    public Object publishOrderEvent(ProceedingJoinPoint joinPoint,
                                    PublishOrderEvent publishOrderEvent) throws Throwable {
        Object result = joinPoint.proceed();
        OrderEventType type = publishOrderEvent.value();

        if (result instanceof OrderResult.PlaceOrderResult order) {
            OrderEventPayload payload = fromOrderResult(type, order);
            log.info("[ORDER][EVENT][AOP] type={}, payload={}", type, payload);
            eventPublisher.publishEvent(payload);
        } else if(result instanceof OrderModel order) {
            OrderEventPayload payload = fromOrderModel(type, order);
            log.info("[ORDER][EVENT][AOP] type={}, payload={}", type, payload);
            eventPublisher.publishEvent(payload);
        } else {
            log.warn("[ORDER][EVENT][AOP] method {} returned non-OrderModel result: {}",
                    joinPoint.getSignature(), result != null ? result.getClass().getName() : "null");
        }

        return result;
    }

    private OrderEventPayload fromOrderResult(OrderEventType type, OrderResult.PlaceOrderResult order) {
        List<Long> productIds =
                Stream.of(order.successLines(), order.failedLines())
                        .flatMap(List::stream)
                        .map(OrderCommand.OrderLine::productId)
                        .collect(Collectors.toList());
        return new OrderEventPayload(
                type,
                order.orderId(),
                order.userId(),
                order.normalPrice(),
                order.orderStatus(),
                productIds,
                Instant.now()
        );
    }

    private OrderEventPayload fromOrderModel(OrderEventType type, OrderModel order) {
        return new OrderEventPayload(
                type,
                order.getId(),
                order.getUser().getUserId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getOrderItems().stream()
                        .map(p->p.getProduct().getId())
                        .collect(Collectors.toList()),
                Instant.now()
        );
    }
}
