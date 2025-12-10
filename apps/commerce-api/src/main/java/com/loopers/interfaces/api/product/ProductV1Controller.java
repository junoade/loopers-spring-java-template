package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductLikeSummary;
import com.loopers.application.product.ProductQueryService;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.tracking.UserActionType;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.tracking.TrackUserAction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec{
    private final ProductQueryService productQueryService;

    @Override
    @GetMapping
    @TrackUserAction(
            actionType = UserActionType.PRODUCT_LIST_VIEW,
            entityType = "PRODUCT"
    )
    public ApiResponse<ProductV1Dto.ProductListResponse<ProductLikeSummary>> getProducts(@RequestParam(required = false, name = "brandId")
                                                                                             Long brandId,
                                            @RequestParam(defaultValue = "DEFAULT", name = "sortType") ProductSortType sortType,
                                            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductLikeSummary> page = productQueryService.getProductListWithLikeCount(brandId, sortType, pageable);
        return ApiResponse.success(ProductV1Dto.ProductListResponse.of(page, page.stream().toList()));
    }

    @Override
    @GetMapping("/{productId}")
    @TrackUserAction(
            actionType = UserActionType.PRODUCT_VIEW,
            entityType = "PRODUCT",
            entityId = "#p0"
    )
    public ApiResponse<ProductV1Dto.ProductDetailResponse<ProductLikeSummary>> getProductDetail(@PathVariable("productId") Long productId) {
        ProductLikeSummary productLikeSummary = productQueryService.getProductLikeSummary(productId);

        return ApiResponse.success(ProductV1Dto.ProductDetailResponse.of(productLikeSummary));
    }
}
