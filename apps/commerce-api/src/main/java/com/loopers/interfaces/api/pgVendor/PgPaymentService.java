package com.loopers.interfaces.api.pgVendor;

import com.loopers.application.order.OrderResult;
import com.loopers.interfaces.api.order.OrderV1Dto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgPaymentService {
    private final PgPaymentRetry pgPaymentRetry;

    /**
     * 비동기 요청 송신 및 Resilience Retry를 AOP로 호출
     * @param orderResult
     * @param request
     * @ref AsyncConfig
     */
    @Async("pgExecutor")
    public void requestPaymentForOrder(OrderResult.PlaceOrderResult orderResult,
                                                          OrderV1Dto.OrderRequest request) {
        PgPaymentV1Dto.Request pgRequest = PgPaymentV1Dto.Request.of(
                "00000" + orderResult.orderId().toString(),
                request.paymentInfo(),
                orderResult.normalPrice(),
                "http://localhost:8080/api/v1/payments/callback"
        );
        pgPaymentRetry.requestPaymentWithRetry(orderResult.userId(), pgRequest);
    }
}
