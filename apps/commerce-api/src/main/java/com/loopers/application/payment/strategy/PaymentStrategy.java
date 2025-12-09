package com.loopers.application.payment.strategy;

import com.loopers.application.payment.config.PaymentFlowType;
import com.loopers.application.payment.dto.PaymentCommand;
import com.loopers.domain.order.OrderModel;

public interface PaymentStrategy {
    /**
     * 전체 결제 프로세스를 처리합니다.
     * 주문 생성 + 포인트 차감 or PG 요청 등)
     * @param context
     * @return OrderModel
     */
    OrderModel processPayment(PaymentCommand.ProcessPaymentContext context);

    PaymentFlowType getType();
}
