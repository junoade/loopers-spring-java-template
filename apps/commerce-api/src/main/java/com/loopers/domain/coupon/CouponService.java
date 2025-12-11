package com.loopers.domain.coupon;

import com.loopers.application.coupon.CouponInfo;
import com.loopers.application.coupon.CreateCouponCommand;
import com.loopers.infrastructure.coupon.CouponRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    @Transactional
    public CouponInfo create(CreateCouponCommand command) {
        couponRepository.findByCouponCode(command.couponCode())
                .ifPresent(c -> {
                    throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 쿠폰 코드입니다. code : " + command.couponCode());
                });

        CouponModel coupon = CouponModel.builder()
                .couponCode(command.couponCode())
                .name(command.name())
                .discountType(command.discountType())
                .discountValue(command.discountValue())
                .status(CouponStatus.ACTIVE)
                .build();

        CouponModel saved = couponRepository.save(coupon);
        return CouponInfo.from(saved);

    }

    /**
     * 쿠폰 단건 조회
     */
    @Transactional
    public CouponModel getCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다. id=" + couponId));

    }

    /**
     * 주문 금액 기준으로 이 쿠폰이 줄 수 있는 할인금액을 반환합니다.
     * - [주의] 할인된 총금액이 아닙니다.
     */
    public long calculateDiscount(Long couponId, long orderAmount) {
        CouponModel coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다. id=" + couponId));
        return coupon.calculateDiscount(orderAmount);
    }

}
