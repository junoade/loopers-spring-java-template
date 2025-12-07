package com.loopers.application.payment.strategy;

import com.loopers.application.order.StockResult;
import com.loopers.application.payment.PaymentFlowType;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.user.UserModel;

import java.util.List;

public interface PaymentStrategy {
    OrderModel createOrder(UserModel user,
                           List<OrderItemModel> items,
                           StockResult stockResult);

    PaymentFlowType getType();
}
