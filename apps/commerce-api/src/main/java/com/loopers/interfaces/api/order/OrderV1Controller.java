package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderResult;
import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.UserOrderProductFacade;
import com.loopers.application.payment.PaymentFlowType;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.pgVendor.PgPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/order")
public class OrderV1Controller implements OrderV1ApiSpec {
    private final UserOrderProductFacade userOrderProductFacade;
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
}
