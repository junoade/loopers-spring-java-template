package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductLikeSummary;
import com.loopers.domain.product.ProductSortType;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

@Tag(name = "상품 관련 V1 API", description = "상품 API")
public interface ProductV1ApiSpec {
    @Operation(
            summary = "상품 목록 조회",
            description = "RequestParams 포맷으로 상품 목록 조회를 처리합니다."
    )
    ApiResponse<ProductV1Dto.ProductListResponse<ProductLikeSummary>>  getProducts(
            @Schema(name="", description = "상품 전체 목록 조회 파라미터")
            Long brandId,
            ProductSortType sortType,
            Pageable pageable
    );

    @Operation(
            summary = "상품 조회",
            description = "PathVariable로 productId를 받아 상품 조회를 처리합니다.."
    )
    ApiResponse<ProductV1Dto.ProductDetailResponse<ProductLikeSummary>>  getProductDetail(
            @Schema(name="", description = "상품 전체 목록 조회 파라미터")
            Long productId
    );

}
