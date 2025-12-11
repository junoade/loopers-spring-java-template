package com.loopers.application.coupon;

import com.loopers.domain.coupon.AssignedCouponModel;
import com.loopers.domain.coupon.AssignedCouponStatus;
import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import com.loopers.infrastructure.coupon.AssignedCouponRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponAssignService {
    private final UserService userService;
    private final CouponService couponService;
    private final AssignedCouponRepository assignedCouponRepository;

    /**
     * 유저에게 쿠폰 발급
     */
    @Transactional
    public AssignedCouponInfo assignCouponToUser(AssignCouponCommand command) {
        UserModel user = userService.getUser(command.userPkId());
        CouponModel coupon = couponService.getCoupon(command.couponId());

        if(!coupon.isStatusActive()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 상태를 확인해주세요.");
        }

        AssignedCouponModel assigned = AssignedCouponModel.builder()
                .user(user)
                .coupon(coupon)
                .status(AssignedCouponStatus.ISSUED)
                .build();

        AssignedCouponModel saved = assignedCouponRepository.save(assigned);
        return AssignedCouponInfo.from(saved);
    }

    /**
     * 쿠폰 사용 처리 + 할인 금액 계산
     */
    @Transactional
    public long useAssignedCoupon(Long assignedCouponId, Long orderId, long orderAmount) {
        AssignedCouponModel assigned = assignedCouponRepository.findById(assignedCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "할당 쿠폰을 찾을 수 없습니다. id=" + assignedCouponId));

        if(!assigned.isStatusUsable()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용불가한 쿠폰입니다.");
        }

        long discountAmount = couponService.calculateDiscount(assigned.getCoupon().getId(), orderAmount);
        if (discountAmount <= 0) {
            throw new IllegalStateException("해당 주문 금액에는 쿠폰을 적용할 수 없습니다.");
        }

        // 사용 처리
        assigned.use(orderId);
        return discountAmount;
    }
}
