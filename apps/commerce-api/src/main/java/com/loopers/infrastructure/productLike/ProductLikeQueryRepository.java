package com.loopers.infrastructure.productLike;

import com.loopers.application.product.ProductLikeSummary;
import com.loopers.domain.like.QProductLikeModel;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.QProductModel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductLikeQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<ProductLikeSummary> findProductLikes(Long brandId,
                                                     ProductSortType sortType,
                                                     Pageable pageable) {

        QProductModel product = QProductModel.productModel;
        QProductLikeModel productLike = QProductLikeModel.productLikeModel;

        BooleanBuilder condition = new BooleanBuilder();
        if(brandId != null) {
            condition.and(product.brand.id.eq(brandId));
        }

        Long total = queryFactory.select(product.id.countDistinct())
                .from(product)
                .leftJoin(productLike).on(productLike.product.eq(product))
                .where(condition)
                .fetchOne();

        if (total == null || total == 0) {
            return Page.empty(pageable);
        }

        List<ProductLikeSummary> content = queryFactory
                .select(Projections.constructor(
                        ProductLikeSummary.class,
                        product.id,
                        product.name,
                        product.brand.id,
                        product.brand.name,
                        product.price,
                        product.status,
                        productLike.id.count()  // likeCount
                ))
                .from(product)
                .leftJoin(productLike).on(productLike.product.eq(product))
                .where(condition)
                .groupBy(
                        product.id,
                        product.name,
                        product.brand.id,
                        product.brand.name,
                        product.price,
                        product.status
                )
                .orderBy(applySort(sortType, productLike))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<ProductLikeSummary> findProductDetail(Long productId) {
        QProductModel product = QProductModel.productModel;
        QProductLikeModel productLike = QProductLikeModel.productLikeModel;

        ProductLikeSummary result = queryFactory
                .select(Projections.constructor(
                        ProductLikeSummary.class,
                        product.id,
                        product.name,
                        product.brand.id,
                        product.brand.name,
                        product.price,
                        product.status,
                        productLike.id.count()
                ))
                .from(product)
                .leftJoin(productLike).on(productLike.product.eq(product))
                .where(
                        product.id.eq(productId)
                )
                .groupBy(
                        product.id,
                        product.name,
                        product.brand.id,
                        product.brand.name,
                        product.price,
                        product.status
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    private OrderSpecifier<?> applySort(ProductSortType sortType, QProductLikeModel productLike) {
        if (sortType == null || sortType == ProductSortType.DEFAULT) {
            return productLike.id.count().desc();
        }

        return switch(sortType) {
            case LIKE_ASC -> productLike.id.count().asc();
            case LIKE_DESC -> productLike.id.count().desc();
            default -> productLike.id.count().desc();
        };
    }

}
