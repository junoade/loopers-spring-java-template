package com.loopers.application.product;

import com.loopers.application.like.ProductLikeQueryRepository;
import com.loopers.domain.product.ProductSortType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductQueryService {
    private final ProductLikeQueryRepository ProductLikeQuery;


    @Transactional(readOnly = true)
    public ProductLikeSummary getProductLikeSummary(Long productId) {
        return ProductLikeQuery.findProductDetail(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 상품입니다"));
    }

    @Transactional(readOnly = true)
    public Page<ProductLikeSummary> getProductListWithLikeCount(Long brandId, ProductSortType sortType, Pageable pageable) {
        return ProductLikeQuery.findProductLikes(brandId, sortType, pageable);
    }
}
