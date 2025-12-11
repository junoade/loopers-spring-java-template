package com.loopers.application.coupon;

import com.loopers.domain.coupon.AssignedCouponModel;
import com.loopers.domain.coupon.AssignedCouponStatus;
import com.loopers.domain.coupon.CouponModel;

public record AssignedCouponInfo(
        Long assignedCouponId,
        Long couponId,
        String couponCode,
        String couponName,
        AssignedCouponStatus status
) {
    public static AssignedCouponInfo from(AssignedCouponModel assigned) {
        CouponModel coupon = assigned.getCoupon();
        return new AssignedCouponInfo(
                assigned.getId(),
                coupon.getId(),
                coupon.getCouponCode(),
                coupon.getName(),
                assigned.getStatus()
        );
    }
}
