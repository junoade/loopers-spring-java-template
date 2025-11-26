package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductQueryService;
import com.loopers.application.product.ProductLikeSummary;
import com.loopers.domain.product.ProductSortType;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec{
    private final ProductQueryService productQueryService;

    @GetMapping
    public ApiResponse<ProductV1Dto.ProductListResponse<ProductLikeSummary>> getProducts(@RequestParam(required = false, name = "brandId")
                                                                                             Long brandId,
                                            @RequestParam(defaultValue = "DEFAULT", name = "sortType") ProductSortType sortType,
                                            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductLikeSummary> page = productQueryService.getProductListWithLikeCount(brandId, sortType, pageable);
        return ApiResponse.success(ProductV1Dto.ProductListResponse.of(page, page.stream().toList()));
    }
}
