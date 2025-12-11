package com.loopers.application.payment.dto;

import com.loopers.application.payment.common.PaymentInfo;

public record PgPaymentCommand(
        String userId,
        Long orderId,
        long amount,
        PaymentInfo paymentInfo,
        String callbackUrl
) {
    public static PgPaymentCommand create(
            String userId,
            Long orderId,
            long amount,
            PaymentInfo paymentInfo,
            String callbackUrl
    ) {
        return new PgPaymentCommand(userId, orderId, amount, paymentInfo, callbackUrl);
    }
}
