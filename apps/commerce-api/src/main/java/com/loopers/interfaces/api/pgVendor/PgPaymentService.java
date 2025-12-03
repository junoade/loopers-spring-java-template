package com.loopers.interfaces.api.pgVendor;

import com.loopers.application.order.OrderResult;
import com.loopers.interfaces.api.order.OrderV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PgPaymentService {
    private final PgClient pgClient;

    public PgPaymentV1Dto.Response requestPaymentForOrder(OrderResult.PlaceOrderResult orderResult,
                                                          OrderV1Dto.OrderRequest request) {
        PgPaymentV1Dto.Request pgRequest = PgPaymentV1Dto.Request.of(
                "00000" + orderResult.orderId().toString(),
                request.paymentInfo(),
                orderResult.normalPrice(),
                "http://localhost:8080/"
        );

        return pgClient.requestPayment(orderResult.userId(), pgRequest);
    }
}
