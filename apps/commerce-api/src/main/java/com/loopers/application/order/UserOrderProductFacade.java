package com.loopers.application.order;

import com.loopers.application.coupon.CouponAssignService;
import com.loopers.application.payment.config.PaymentStrategyResolver;
import com.loopers.application.payment.dto.PaymentCommand;
import com.loopers.application.payment.strategy.PaymentStrategy;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemStatus;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.order.event.OrderEventType;
import com.loopers.support.tracking.annotation.PublishOrderEvent;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserOrderProductFacade {
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final CouponService couponService;

    private final PaymentStrategyResolver paymentStrategyResolver;
    private final CouponAssignService couponAssignService;

    @Transactional(readOnly = true)
    public OrderResult.PreOrderResult preOrder(OrderCommand.Order orderCommand) {
        UserModel userModel = userService.getUser(orderCommand.userId());
        StockResult result = readAllStocks(orderCommand.orderLineRequests());
        if(!userModel.hasEnoughPoint(result.requiringPrice())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }

        return OrderResult.PreOrderResult.of(
                userModel.getUserId(),
                result.requiringPrice(),
                result.successLines(),
                result.failedLines()
        );
    }

    /**
     * [요구사항] 주문 전체 흐름에 대한 원자성이 보장
     * - 재고가 존재하지 않거나 부족할 경우 주문은 실패
     * - 주문 시 유저의 결제방식은 결제 전략 패턴을 통해 처리합니다
     * - 쿠폰, 재고, 포인트 처리 등 하나라도 작업이 실패하면 모두 롤백처리
     * [변경사항]
     * - [25.12.10] 주문 상태에 관한 이벤트를 AOP로 처리합니다.
     * - [25.12.12] 쿠폰을 통한 할인된 금액이 0원이라면 PG 결제로 요청하더라도 내부 포인트 결제로 처리합니다.
     *
     * [참고] PaymentStrategy 패턴을 사용합니다.
     * - POINT_ONLY, PG_ONLY 등의 팡식을 동적으로 결정합니다.
     * - PG 거래가 껴있는 경우 이벤트 기반으로 PG 결제 요청까지 처리합니다.
     * @param orderCommand
     */
    @Transactional
    @PublishOrderEvent(OrderEventType.ORDER_CREATED)
    public OrderResult.PlaceOrderResult placeOrder(OrderCommand.Order orderCommand) {

        List<OrderItemModel> orderItems = toDomainOrderItem(orderCommand.orderLineRequests());
        UserModel userModel = userService.getUser(orderCommand.userId());
        StockResult stockResult = decreaseAllStocks(orderItems);

        validateStock(stockResult);
        stockResult = discountByCouponWhenGivens(orderCommand, stockResult);

        PaymentStrategy paymentStrategy = paymentStrategyResolver.resolveRequiringPoints(
                orderCommand.paymentFlowType(),
                stockResult.requiringPrice());
        PaymentCommand.ProcessPaymentContext contextDto = PaymentCommand.ProcessPaymentContext.of(
                userModel, orderItems, stockResult, orderCommand.paymentInfo()
        );
        OrderModel orderModel = paymentStrategy.processPayment(contextDto);

        return OrderResult.PlaceOrderResult.of(
                userModel.getUserId(),
                orderModel.getId(),
                stockResult.requiringPrice(),
                stockResult.errorPrice(),
                orderModel.getStatus(),
                stockResult.successLines(),
                stockResult.failedLines(),
                orderCommand.couponId()
        );
    }

    private void validateStock(StockResult stockResult) {
        boolean hasOutOfStockCase = !stockResult.failedLines().isEmpty();
        if(hasOutOfStockCase) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다. 다시 확인해주세요");
        }
    }

    /**
     * 쿠폰ID가 전달되지 않은 경우는 에러를 반환하지 않으나,
     * 쿠폰ID가 전달된 경우라면 유효한 쿠폰인지 검증하고 예외를 반환합니다.
     * @param orderCommand
     * @param stockResult
     */
    private StockResult discountByCouponWhenGivens(OrderCommand.Order orderCommand, StockResult stockResult) {
        if(orderCommand.couponId() == null) {
            return stockResult;
        }

        couponAssignService.validateAssignedCoupon(orderCommand.couponId(), orderCommand.userId());
        long discountAmount = couponService.calculateDiscount(orderCommand.couponId(), stockResult.requiringPrice());
        long finalPaymentAmount = stockResult.requiringPrice() - discountAmount;
        if(finalPaymentAmount < 0) {
            finalPaymentAmount  = 0;
        }

        return stockResult.withRequiringPrice(finalPaymentAmount);
    }

    @Transactional
    protected StockResult decreaseAllStocks(List<OrderItemModel> items) {
        List<OrderCommand.OrderLine> success = new ArrayList<>();
        List<OrderCommand.OrderLine> failed = new ArrayList<>();
        int total = 0, fail = 0;

        /* productId 순서로 정렬 - productService 내부에서 다시 조회하거나 flush 타이밍에 UPDATE가 걸릴 수도 있음.*/
        /*List<OrderItemModel> sortedItems = items.stream()
                .sorted(Comparator.comparing(o -> o.getProduct().getId()))
                .toList();
        List<Long> productIds = sortedItems.stream()
                .map(i -> i.getProduct().getId())
                .toList();*/

        for (OrderItemModel item : items) {
            ProductModel p = item.getProduct();
            boolean ok = productService.decreaseStock(p.getId(), item.getQuantity());
            OrderCommand.OrderLine line = OrderCommand.OrderLine.from(item);
            int requiringPoint = productService.getPrice(p.getId(), item.getQuantity());
            if (ok) {
                item.setStatus(OrderItemStatus.SUCCESS);
                total += requiringPoint;
                success.add(line);
            } else {
                item.setStatus(OrderItemStatus.FAILED);
                fail += requiringPoint;
                failed.add(line);
            }
        }

        return StockResult.of(success, failed, total, fail);
    }

    protected StockResult readAllStocks(List<OrderCommand.OrderLine> lines) {
        List<OrderCommand.OrderLine> success = new ArrayList<>();
        List<OrderCommand.OrderLine> failed = new ArrayList<>();
        int requiringPrice = 0, errorPrice = 0;

        for (OrderCommand.OrderLine line : lines) {
            // TODO 엔티티 클래스에서 예외 발생시 포인트 계산 제대로 되는지 확인 필요
            boolean ok = productService.hasStock(line.productId(), line.quantity());
            int point = productService.getPrice(line.productId(), line.quantity());
            if (ok) {
                success.add(line);
                requiringPrice += point;
            } else {
                failed.add(line);
                errorPrice += point;
            }
        }
        return StockResult.of(success, failed, requiringPrice, errorPrice);
    }


    public List<OrderItemModel> toDomainOrderItem(List<OrderCommand.OrderLine> lines) {
        return lines.stream()
                .map(l -> {
                    ProductModel p = productService.getProductDetail(l.productId());
                    return OrderItemModel.of(p, l.quantity());
                })
                .toList();

    }


    @Transactional(readOnly = true)
    public OrderResult.PlaceOrderResult getOrderResult(Long orderId) {
        OrderModel orderModel = orderService.getOrder(orderId);

        List<OrderCommand.OrderLine> successLines = new ArrayList<>();
        List<OrderCommand.OrderLine> failedLines = new ArrayList<>();

        for(OrderItemModel item : orderModel.getOrderItems()) {
            if(item.getStatus() == OrderItemStatus.SUCCESS) {
                successLines.add(OrderCommand.OrderLine.from(item));
            } else {
                failedLines.add(OrderCommand.OrderLine.from(item));
            }
        }

        return OrderResult.PlaceOrderResult.of(
                orderModel.getUser().getUserId(),
                orderModel.getId(),
                orderModel.getNormalPrice(),
                orderModel.getErrorPrice(),
                orderModel.getStatus(),
                successLines,
                failedLines,
                null
        );
    }
}
