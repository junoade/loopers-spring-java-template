package com.loopers.application.payment;

import com.loopers.application.payment.dto.PgPaymentCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgPaymentService {
    private final PgPaymentRetry pgPaymentRetry;

    /**
     * 비동기 요청 송신 및 Resilience Retry를 AOP로 bypass호출
     * @param command
     * @ref AsyncConfig
     */
    @Async("pgExecutor")
    public void requestPaymentForOrder(PgPaymentCommand command) {
        pgPaymentRetry.requestPaymentWithRetry(command);
    }

    /**
     * 동기 처리 및 Resilience Retry를 AOP로 호출
     * @param userId
     * @param orderId
     */
    public void requestPaymentForPendingOrder(String userId, Long orderId) {
        String paddingId = "00000" + orderId.toString();
        pgPaymentRetry.requestPaymentForPendingOrder(userId, paddingId);
    }

}
