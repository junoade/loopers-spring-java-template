package com.loopers.application.like;

import com.loopers.application.product.ProductLikeSummary;
import com.loopers.domain.product.ProductSortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductLikeQueryRepository {
    Page<ProductLikeSummary> findProductLikes(Long brandId,
                                              ProductSortType sortType,
                                              Pageable pageable);
    Optional<ProductLikeSummary> findProductDetail(Long productId);
}
