package com.loopers.interfaces.api.pgVendor;

import com.loopers.application.order.OrderCommand;
import com.loopers.application.payment.PaymentFlowType;
import com.loopers.application.payment.PaymentInfo;
import com.loopers.interfaces.api.order.OrderV1Dto;

import java.util.List;

public class PgPaymentV1Dto {

    public record Request (
        String orderId,
        String cardType,
        String cardNo,
        long amount,
        String callbackUrl
    ) {
        public static PgPaymentV1Dto.Request of(
                String orderId,
                PaymentInfo paymentInfo,
                long amount,
                String callbackUrl) {
            return new PgPaymentV1Dto.Request(
                    orderId,
                    paymentInfo.cardType(),
                    paymentInfo.cardNo(),
                    amount,
                    callbackUrl
            );
        }
    }

    public record Response (
            String transactionKey,
            String status,
            String reason
    ) { }
}
