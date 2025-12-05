package com.loopers.domain.order;

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
    public void updateOrderAsFailed(Long orderId, int errorPrice) {
        OrderModel orderModel = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        orderModel.updateToFailed(errorPrice);
    }

    @Transactional
    public void updateOrderAsFailed(Long orderId) {
        OrderModel orderModel = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        orderModel.updateToFailed(orderModel.getTotalPrice());
    }

    @Transactional(readOnly = true)
    public boolean isPending(Long orderId) {
        OrderModel orderModel = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        return orderModel.getStatus() == OrderStatus.PENDING;
    }

    @Transactional
    public void updateOrderAsSuccess(Long orderId, int normalPrice) {
        OrderModel orderModel = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        orderModel.updateToFailed(normalPrice);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        OrderModel orderModel = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        orderModel.updateStatus(status);
    }

    @Transactional
    public OrderModel createSuccessOrder(UserModel userModel, List<OrderItemModel> orderItems, int normalPrice) {
        OrderModel orderModel = OrderModel.createSuccess(userModel, orderItems, normalPrice);
        return orderRepository.save(orderModel);
    }

    @Transactional(readOnly = true)
    public OrderModel getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
    }
}
