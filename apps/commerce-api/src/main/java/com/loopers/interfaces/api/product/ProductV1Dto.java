package com.loopers.interfaces.api.product;

import com.loopers.ranking.RankingEntry;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.OptionalDouble;

public class ProductV1Dto {

    public record ProductListResponse<T>(
            List<T> content,
            int page,          // 현재 페이지 번호
            int size,          // 페이지 크기
            long totalElements,
            int totalPages,
            boolean last
    ) {
        public static <T> ProductListResponse<T> of(Page<T> page, List<T> content) {
            return new ProductListResponse<>(
                    content,
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast()
            );
        }

    }

    public record ProductDetailResponse<T>(
            T content,
            OptionalDouble rankingScore
    ){
        static <T> ProductDetailResponse<T> of(T content) {
            return new ProductDetailResponse<>(content, null);
        }

        static <T> ProductDetailResponse<T> of(T content, OptionalDouble rankingScore) {
            return new ProductDetailResponse<>(content, rankingScore);
        }
    }
}
