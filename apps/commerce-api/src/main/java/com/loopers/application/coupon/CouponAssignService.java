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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
     * 쿠폰 사용 처리
     * - 사전에 교부된 쿠폰번호에 대한 검증을 했다고 가정합니다.
     */
    @Transactional
    public void useAssignedCoupon(Long assignedCouponId, Long orderId) {
        AssignedCouponModel assigned = assignedCouponRepository.findById(assignedCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "할당 쿠폰을 찾을 수 없습니다. id=" + assignedCouponId));

        if(!assigned.isStatusUsable()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용불가한 쿠폰입니다.");
        }

        assigned.use(orderId);
        assignedCouponRepository.save(assigned);
    }

    @Transactional(readOnly = true)
    public void validateAssignedCoupon(Long assignedCouponId, String userId) {
        UserModel user = userService.getUser(userId);
        AssignedCouponModel assigned = assignedCouponRepository.findById(assignedCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "할당 쿠폰을 찾을 수 없습니다. id=" + assignedCouponId));

        if(!assigned.isStatusUsable()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용불가한 쿠폰입니다.");
        }

        if(assigned.getUser().getId().longValue() != user.getId().longValue()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 정보를 다시 확인해주세요.");
        }
    }
}
