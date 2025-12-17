package com.loopers.application.payment.strategy;

import com.loopers.application.payment.config.PaymentFlowType;
import com.loopers.application.payment.dto.PaymentCommand;
import com.loopers.application.payment.dto.PgPaymentCommand;
import com.loopers.application.payment.event.PgPaymentRequestedEvent;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PgPaymentStrategy implements PaymentStrategy {
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * PG사를 통한 결제의 경우 내부 포인트 금액과 상관없이 처리합니다.
     * PG사의 경우 대외거래가 포함되므로 OrderStatus.PENDING으로 주문정보를 생성합니다.
     * PG사 외부 연동 거래를 비동기 이벤트를 발행합니다.
     * @param context
     * @return OrderModel
     */
    @Override
    public OrderModel processPayment(PaymentCommand.ProcessPaymentContext context) {
        // UserModel user, List<OrderItemModel> items, StockResult stockResult
        UserModel user = context.userModel();
        List<OrderItemModel> items = context.items();
        OrderModel pendingOrder = orderService.createPendingOrder(user, items);

        if(context.paymentInfo() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "No payment info provided");
        }

        PgPaymentCommand paymentCommand = PgPaymentCommand.create(
                user.getUserId(),
                pendingOrder.getId(),
                context.stockResult().requiringPrice(),
                context.paymentInfo(),
                null
        );

        eventPublisher.publishEvent(new PgPaymentRequestedEvent(paymentCommand));
        return pendingOrder;
    }

    @Override
    public PaymentFlowType getType() {
        return PaymentFlowType.PG_ONLY;
    }
}
