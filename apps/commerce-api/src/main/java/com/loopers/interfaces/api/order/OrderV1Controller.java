package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderResult;
import com.loopers.application.order.UserOrderProductFacade;
import com.loopers.application.payment.PaymentFlowType;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.pgVendor.PgPaymentService;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/order")
public class OrderV1Controller implements OrderV1ApiSpec {
    private final UserOrderProductFacade userOrderProductFacade;
    private final OrderService orderService;
    private final PgPaymentService pgPaymentService;

    @PostMapping("/preOrder")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> preOrder(@RequestBody OrderV1Dto.OrderRequest request) {
        OrderCommand.Order orderCmd = request.toCommand();
        OrderResult.PreOrderResult preOrderResult = userOrderProductFacade.preOrder(orderCmd);
        return ApiResponse.success(OrderV1Dto.OrderResponse.fromPreOrder(preOrderResult));
    }

    @PostMapping("/placeOrder")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> placeOrder(@RequestBody OrderV1Dto.OrderRequest request) {
        OrderCommand.Order orderCmd = request.toCommand();
        OrderResult.PlaceOrderResult placeOrderResult = userOrderProductFacade.placeOrder(orderCmd);

        /**
         * PG 결제인 경우
         */
        if(request.paymentFlowType().equals(PaymentFlowType.PG_ONLY)) {
            pgPaymentService.requestPaymentForOrder(placeOrderResult, request);
        }

        return ApiResponse.success(OrderV1Dto.OrderResponse.fromOrderPlacement(
                placeOrderResult
        ));
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> getOrderDetails(@PathVariable("orderId") Long orderId) {
        OrderResult.PlaceOrderResult orderResult = userOrderProductFacade.getOrderResult(orderId);
        return ApiResponse.success(
                OrderV1Dto.OrderResponse.fromOrderPlacement(orderResult)
        );
    }

    @PutMapping("/{orderId}/payment/pendingRetry")
    @Override
    public ApiResponse<?> checkOrderStatus(@PathVariable("orderId") Long orderId) {
        OrderResult.PlaceOrderResult orderResult = userOrderProductFacade.getOrderResult(orderId);
        if(orderResult.orderStatus() != OrderStatus.PENDING) {
            return ApiResponse.fail(ErrorType.BAD_REQUEST.getCode(), "처리중인 주문상품만 입력해주세요");
        }
        pgPaymentService.requestPaymentForPendingOrder(orderResult.userId(), orderResult.orderId());

        OrderResult.PlaceOrderResult result = userOrderProductFacade.getOrderResult(orderId);
        return ApiResponse.success(
                OrderV1Dto.OrderResponse.fromOrderPlacement(result)
        );
    }

}
