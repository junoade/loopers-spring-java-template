package com.loopers.application.payment.strategy;

import com.loopers.application.order.StockResult;
import com.loopers.application.payment.config.PaymentFlowType;
import com.loopers.application.payment.dto.PaymentCommand;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PointPaymentStrategy implements PaymentStrategy {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * 내부 포인트 결제이고 주문 과정이 정상이면 주문은 바로 성공합니다
     * @param context
     * @return OrderModel
     */
    @Override
    public OrderModel processPayment(PaymentCommand.ProcessPaymentContext context) {
        UserModel user = context.userModel();
        List<OrderItemModel> items = context.items();
        StockResult stockResult = context.stockResult();

        int requiringPoints = stockResult.requiringPrice();

        if (!user.hasEnoughPoint(requiringPoints)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다. 다시 확인해주세요!");
        }
        userService.decreaseUserPoint(user.getId(), requiringPoints);

        return orderService.createSuccessOrder(user, items, requiringPoints);
    }

    @Override
    public PaymentFlowType getType() {
        return PaymentFlowType.POINT_ONLY;
    }
}
