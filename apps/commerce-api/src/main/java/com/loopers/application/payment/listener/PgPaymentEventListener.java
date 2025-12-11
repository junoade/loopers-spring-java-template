package com.loopers.application.payment.listener;

import com.loopers.application.payment.PgPaymentService;
import com.loopers.application.payment.event.PgPaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentEventListener {
    private final PgPaymentService pgPaymentService;

    /**
     * 주문 트랜잭션이 커밋된 후 비동기로 PG 결제 요청
     */
    @Async("pgExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handlePgPaymentRequested(PgPaymentRequestedEvent event) {

        log.info("[PG][EVENT] PgPaymentRequestedEvent received. userId={}, orderId={}",
                event.command().userId(), event.command().orderId());

        // 실제 PG 호출은 서비스에 위임
        pgPaymentService.requestPaymentForOrder(event.command());
    }
}
