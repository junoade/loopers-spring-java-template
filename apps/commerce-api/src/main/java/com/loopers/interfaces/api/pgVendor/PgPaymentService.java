package com.loopers.interfaces.api.pgVendor;

import com.loopers.application.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.interfaces.api.order.OrderV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PgPaymentService {
    private final PgClient pgClient;
    private final OrderService orderService;

    /**
     * 비동기 요청 송신
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

        try {
            PgPaymentV1Dto.Response pgRes = pgClient.requestPayment(orderResult.userId(), pgRequest);
        } catch (Exception e) {
            // [TODO] PG 요청 자체가 실패한 경우 retry 3번 후 그래도 실패하면 주문 상태를 FAILED 로 갱신
            orderService.updateOrderAsFailed(orderResult.orderId(), orderResult.errorPrice());
        }
    }
}
