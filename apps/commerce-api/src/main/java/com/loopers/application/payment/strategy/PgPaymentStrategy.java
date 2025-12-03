package com.loopers.application.payment.strategy;

import com.loopers.application.order.StockResult;
import com.loopers.application.payment.PaymentFlowType;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PgPaymentStrategy implements PaymentStrategy {
    private final OrderService orderService;

    /**
     * PG사를 통한 결제의 경우 내부 포인트 금액과 상관없이 처리합니다.
     * PG사의 경우 대외거래가 포함되므로 OrderStatus.PENDING으로 주문정보를 생성합니다.
     * @param user
     * @param items
     * @param stockResult
     * @return
     */
    @Override
    public OrderModel createOrder(UserModel user, List<OrderItemModel> items, StockResult stockResult) {
        return orderService.createPendingOrder(user, items);
    }

    @Override
    public PaymentFlowType getType() {
        return PaymentFlowType.PG_ONLY;
    }
}
