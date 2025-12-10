package com.loopers.application.product;

import com.loopers.application.like.ProductLikeQueryRepository;
import com.loopers.application.tracking.UserActionPublisher;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.tracking.EventActor;
import com.loopers.domain.tracking.EventActorType;
import com.loopers.domain.tracking.UserActionEvent;
import com.loopers.domain.tracking.UserActionType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductQueryService {
    private final ProductLikeQueryRepository ProductLikeQuery;
    private final UserActionPublisher userActionPublisher;

    @Transactional(readOnly = true)
    public ProductLikeSummary getProductLikeSummary(Long productId) {
        ProductLikeSummary summary = ProductLikeQuery.findProductDetail(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다"));

        UserActionEvent event = new UserActionEvent(
                UUID.randomUUID().toString(),
                EventActorType.USER,
                UserActionType.PRODUCT_VIEW,
                "PRODUCT",
                productId.toString(),
                "WEB",
                "",
                MDC.get("traceId"),
                Instant.now(),
                Map.of(
                        "brandId", summary.getBrandId().toString(),
                        "price", summary.getPrice()
                )
        );
        userActionPublisher.publish(event);

        return summary;
    }

    @Cacheable(
            cacheNames = "productLikeSummary",
            key = "T(String).valueOf(#brandId ?: 'ALL')" +
                    " + ':' + (#p1 != null ? #p1.name() : 'DEFAULT')" +
                    " + ':' + (#p2 != null ? #p2.pageNumber : 0)" +
                    " + ':' + (#p3 != null ? #p3.pageSize : 20)"
    )
    @Transactional(readOnly = true)
    public Page<ProductLikeSummary> getProductListWithLikeCount(Long brandId, ProductSortType sortType, Pageable pageable) {
        return ProductLikeQuery.findProductLikes(brandId, sortType, pageable);
    }

}
