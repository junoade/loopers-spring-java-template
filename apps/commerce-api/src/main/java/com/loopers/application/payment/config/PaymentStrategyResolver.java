package com.loopers.application.payment.config;

import com.loopers.application.payment.strategy.PaymentStrategy;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentStrategyResolver {
    private final List<PaymentStrategy> strategies;

    public PaymentStrategy resolve(PaymentFlowType paymentFlowType) {
        return strategies.stream()
                .filter(s -> s.getType() == paymentFlowType)
                .findFirst()
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "올바르지 않은 결제 방식입니다."));
    }
}
