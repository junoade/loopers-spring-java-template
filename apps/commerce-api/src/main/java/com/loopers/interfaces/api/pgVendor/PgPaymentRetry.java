package com.loopers.interfaces.api.pgVendor;

import com.loopers.domain.order.OrderService;
import feign.FeignException;
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
     * @ref application.yml
     * @param request
     * @return
     */
    @Retry(name = "pgPayment", fallbackMethod = "pgPaymentFallback")
    public void requestPaymentWithRetry(String userId, PgPaymentV1Dto.Request request) {
        pgClient.requestPayment(userId, request);
    }

    public void pgPaymentFallback(String userId, PgPaymentV1Dto.Request request, Throwable ex) {
        log.error("[PG PAYMENT] all retries failed. userId={}, orderId={}, cause={}",
                userId, request.orderId(), ex.toString());

        // long parseOrderId = Long.parseLong(request.orderId());
        // orderService.updateOrderAsFailed(parseOrderId, request.amount());
        // 처리중으로 그대로 남깁니다.
        // 추후 별도의 테이블로 관리할 수도 있겠습니다.
    }


    @Retry(name = "pgPayment", fallbackMethod = "pgPaymentOrderStatusFallback")
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


}
