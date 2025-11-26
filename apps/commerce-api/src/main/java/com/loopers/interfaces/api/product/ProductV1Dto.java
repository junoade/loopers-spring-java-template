package com.loopers.interfaces.api.product;

import org.springframework.data.domain.Page;

import java.util.List;

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
}
