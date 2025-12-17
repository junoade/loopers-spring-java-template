package com.loopers.application.coupon.listener;

import com.loopers.application.coupon.CouponAssignService;
import com.loopers.domain.order.event.OrderEventPayload;
import com.loopers.domain.order.event.OrderEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUseEventListener {
    private final CouponAssignService couponAssignService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCouponUseEvent(OrderEventPayload payload) {
        if(payload.eventType() != OrderEventType.ORDER_CREATED || payload.couponId() == null) {
            return;
        }
        log.debug("[ORDER][EVENT][CouponUseEventListener] 쿠폰사용을 처리합니다. type={}, payload={}", payload.eventType(), payload);
        Long usedCouponId = payload.couponId();
        Long orderId = payload.orderId();
        couponAssignService.useAssignedCoupon(usedCouponId, orderId);
    }
}
