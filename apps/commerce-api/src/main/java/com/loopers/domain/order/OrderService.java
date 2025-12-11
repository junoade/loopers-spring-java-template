package com.loopers.domain.order;

import com.loopers.domain.tracking.order.OrderEventType;
import com.loopers.domain.tracking.order.PublishOrderEvent;
import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderModel createPendingOrder(UserModel userModel, List<OrderItemModel> orderItems) {
        OrderModel orderModel = OrderModel.createPending(userModel, orderItems);
        return orderRepository.save(orderModel);
    }

    @Transactional
    @PublishOrderEvent(OrderEventType.ORDER_FAILED)
    public OrderModel updateOrderAsFailed(Long orderId, long errorPrice) {
        OrderModel orderModel = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        orderModel.updateToFailed(errorPrice);
        return orderModel;
    }

    @Transactional
    @PublishOrderEvent(OrderEventType.ORDER_FAILED)
    public OrderModel updateOrderAsFailed(Long orderId) {
        OrderModel orderModel = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        orderModel.updateToFailed(orderModel.getTotalPrice());
        return orderModel;
    }

    @Transactional(readOnly = true)
    public boolean isPending(Long orderId) {
        OrderModel orderModel = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        return orderModel.getStatus() == OrderStatus.PENDING;
    }

    @Transactional
    @PublishOrderEvent(OrderEventType.ORDER_PAID)
    public OrderModel updateOrderAsSuccess(Long orderId, long normalPrice) {
        OrderModel orderModel = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        orderModel.updateToSuccess(normalPrice);
        return orderModel;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        OrderModel orderModel = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        orderModel.updateStatus(status);
    }

    @Transactional
    public OrderModel createSuccessOrder(UserModel userModel, List<OrderItemModel> orderItems, long normalPrice) {
        OrderModel orderModel = OrderModel.createSuccess(userModel, orderItems, normalPrice);
        return orderRepository.save(orderModel);
    }

    @Transactional(readOnly = true)
    public OrderModel getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
    }
}
