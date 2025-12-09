package com.loopers.application.payment;

import com.loopers.application.payment.dto.PgPaymentCommand;
import com.loopers.domain.order.OrderService;
import com.loopers.infrastructure.pg.PgClient;
import com.loopers.interfaces.api.payment.PgPaymentV1Dto;
import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgPaymentRetry {
    private final PgClient pgClient;
    private final OrderService orderService;

    /**
     * PG 요청에 대해 Resilience4j Retry 로직
     * 서킷브레이커 적용
     * @ref application.yml
     * @param command
     * @return
     */
    @Retry(name = "pgPayment", fallbackMethod = "pgPaymentFallback")
    @CircuitBreaker(name = "pgCircuit", fallbackMethod = "pgPaymentFallback")
    public void requestPaymentWithRetry(PgPaymentCommand command) {
        PgPaymentV1Dto.Request pgRequest = PgPaymentV1Dto.Request.of(
                "00000" + command.orderId().toString(),
                command.paymentInfo(),
                command.amount(),
                "http://localhost:8080/api/v1/payments/callback"
        );

        pgClient.requestPayment(command.userId(), pgRequest);
    }

    public void pgPaymentFallback(PgPaymentCommand command, Throwable ex) {
        log.error("[PG PAYMENT] all retries failed. userId={}, orderId={}, cause={}",
                command.userId(), command.orderId(), ex.toString());

        // long parseOrderId = Long.parseLong(request.orderId());
        // orderService.updateOrderAsFailed(parseOrderId, request.amount());
        // 처리중으로 그대로 남깁니다.
        // 추후 별도의 테이블로 관리할 수도 있겠습니다.
    }


    @Retry(name = "pgPayment", fallbackMethod = "pgPaymentOrderStatusFallback")
    @CircuitBreaker(name = "pgCircuit", fallbackMethod = "pgPaymentOrderStatusCircuitBreakerFallback")
    public void requestPaymentForPendingOrder(String userId, String orderId) {
        log.debug("[PG PAYMENT] called for orderId={}", orderId);
        pgClient.requestPaymentWithOrderId(userId, orderId);
    }

    public void pgPaymentOrderStatusFallback(String userId, String orderId, Throwable ex) {
        log.error("[PG PAYMENT] all retries failed. userId={}, orderId={}, cause={}",
                userId, orderId, ex.toString());

        // 내부 시스템 원장에만 처리중으로 남고 / PG사 원거래 조회시 거래 건 없음
        if (ex instanceof FeignException fe) {
            if (fe.status() == 404) {
                log.error("[PG PAYMENT] 404 NOT FOUND → 결제 실패로 처리합니다. userId={}, orderId={}", userId, orderId);
                long parseOrderId = Long.parseLong(orderId);
                orderService.updateOrderAsFailed(parseOrderId);
            }
        }

    }

    public void pgPaymentOrderStatusCircuitBreakerFallback(String userId, String orderId, Throwable ex) {
        if (ex instanceof CallNotPermittedException) {
            log.warn("[PG][CIRCUIT_OPEN] call blocked. userId={}, orderId={}, cause={}",
                    userId, orderId, ex.toString());
            return;
        }

        if (ex instanceof FeignException fe) {
            int status = fe.status();
            if (status == 404) {
                log.error("[PG][HTTP_404] transaction not found. userId={}, orderId={}, msg={}",
                        userId, orderId, fe.toString());
                // 여기서 주문 FAIL 처리 같은 보상 로직
                // orderService.updateOrderAsFailed(...);
            } else if (status >= 500) {
                log.error("[PG][HTTP_5XX] server error. userId={}, orderId={}, status={}, msg={}",
                        userId, orderId, status, fe.toString());
            } else {
                log.warn("[PG][HTTP_4XX] client error. userId={}, orderId={}, status={}, msg={}",
                        userId, orderId, status, fe.toString());
            }
            return;
        }

        // 타임아웃/네트워크 오류 (RetryableException 등)
        if (ex instanceof RetryableException) {
            log.error("[PG][RETRYABLE] network/timeout error. userId={}, orderId={}, msg={}",
                    userId, orderId, ex.getMessage(), ex);
            return;
        }

        // 4) 그 외 알 수 없는 에러
        log.error("[PG][UNKNOWN] unexpected error. userId={}, orderId={}, type={}, msg={}",
                userId, orderId, ex.getClass().getSimpleName(), ex.getMessage(), ex);
    }


}
