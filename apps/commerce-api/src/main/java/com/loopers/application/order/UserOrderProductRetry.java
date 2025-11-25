package com.loopers.application.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserOrderProductRetry {
    private final UserOrderProductFacade userOrderProductFacade;

    /**
     * retry-wrapper
     * 트랜잭션 분리를 위해 별도의 프록시 객체를 거쳐 호출하여 트랜잭션을 분리합니다.
     * - @Transactional 적용 조건 : 프록시를 통해 호출 될때만, 같은 클래스 내부의 메소드 호출은 미적용
     * - 프록시는 트랜잭션 시작, 커밋과 롤백의 제어, 예외 변환을 담당한다.
     * - 키워드 : Spring 트랜잭션 경계 원리
     * @param orderCommand
     * @return
     */
    public OrderResult.PlaceOrderResult placeOrderWithRetry(OrderCommand.Order orderCommand) throws CannotAcquireLockException {
        int maxRetry = 3;

        for (int i = 0; i < maxRetry; i++) {
            try {
                return userOrderProductFacade.placeOrder(orderCommand);
            } catch (CannotAcquireLockException e) {
                log.warn("Deadlock detected. retry={}", i + 1);
                try {
                    Thread.sleep(10L * (i + 1)); // backoff
                } catch (InterruptedException e2) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e2);
                }
            }
        }

        throw new CoreException(ErrorType.BAD_REQUEST, "일시적인 오류입니다. 다시 시도해주세요.");
    }
}
