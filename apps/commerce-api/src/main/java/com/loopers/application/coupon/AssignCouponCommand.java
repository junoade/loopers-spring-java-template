package com.loopers.application.coupon;

public record AssignCouponCommand(
        Long couponId,
        Long userPkId
) {
}
