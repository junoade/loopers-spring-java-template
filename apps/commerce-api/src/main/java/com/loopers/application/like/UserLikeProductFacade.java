package com.loopers.application.like;

import com.loopers.application.like.event.LikeEventType;
import com.loopers.application.like.event.ProductLikeEvent;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserLikeProductFacade {
    private final UserService userService;
    private final ProductLikeService productLikeService;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    @CacheEvict(cacheNames = "productLikeSummary", allEntries = true)
    @Transactional
    public void userLikeProduct(LikeCommand.Like input) {
        UserModel found = userService.getUser(input.userId());
        ProductModel product = productService.getProductDetail(input.productId());
        productService.validateProductDeleteOrStopSelling(product);
        int resultRow = productLikeService.like(found.getId(), input.productId());
        if(resultRow == 1) {
            ProductLikeEvent userLikeEvent = ProductLikeEvent.from(found.getId(), input.productId(), LikeEventType.LIKE_CREATED);
            eventPublisher.publishEvent(userLikeEvent);
        }
    }

    @CacheEvict(cacheNames = "productLikeSummary", allEntries = true)
    @Transactional
    public void userUnlikeProduct(LikeCommand.Like input) {
        UserModel found = userService.getUser(input.userId());
        ProductModel product = productService.getProductDetail(input.productId());
        productService.validateProductDeleteOrStopSelling(product);
        int resultRow = productLikeService.dislike(found.getId(), product.getId());
        if(resultRow == 1) {
            ProductLikeEvent userLikeEvent = ProductLikeEvent.from(found.getId(), input.productId(), LikeEventType.LIKE_DELETED);
            eventPublisher.publishEvent(userLikeEvent);
        }
    }

}
