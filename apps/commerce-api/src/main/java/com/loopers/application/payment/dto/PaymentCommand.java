package com.loopers.application.payment.dto;

import com.loopers.application.order.StockResult;
import com.loopers.application.payment.common.PaymentInfo;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.user.UserModel;
import jakarta.annotation.Nullable;

import java.util.List;

public class PaymentCommand {

    /**
     * PaymentStrategy 패턴에서 입력 매개변수를 관리합니다
     * @param userModel
     * @param items
     * @param stockResult
     * @param paymentInfo
     */
    public record ProcessPaymentContext(
            UserModel userModel,
            List<OrderItemModel> items,
            StockResult stockResult,
            @Nullable PaymentInfo paymentInfo
    ) {
        public static ProcessPaymentContext of(
                UserModel userModel,
                List<OrderItemModel> items,
                StockResult stockResult,
                @Nullable PaymentInfo paymentInfo
        ) {
            return new ProcessPaymentContext(
                    userModel, items, stockResult, paymentInfo
            );
        }
    }
}
