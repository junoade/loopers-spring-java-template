package com.loopers.application.like.event;

import com.loopers.domain.product.ProductService;
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
public class LikeEventListener {
    private final ProductService productService;

    /**
     * UserLikeProductFacade 서비스에서 발행한 상품좋아요/상품 싫어요 이벤트를 처리합니다.
     * @param event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductLikeEvent(ProductLikeEvent event) {
        log.debug("Like event received: {}", event);

        if(event.eventType() == LikeEventType.LIKE_CREATED) {
            productService.increaseProductLikeCount(event.productId());
        } else if(event.eventType() == LikeEventType.LIKE_DELETED) {
            productService.decreaseProductLikeCount(event.productId());
        } else {
            log.info("[info] - Duplicated Like event received: {}", event);
        }
    }
}
