package com.loopers.infrastructure.productLike;

import com.loopers.application.like.ProductLikeQueryRepository;
import com.loopers.application.product.ProductLikeSummary;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.QProductModel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Primary
@Component
@RequiredArgsConstructor
public class ProductLikeQueryV2Impl implements ProductLikeQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductLikeSummary> findProductLikes(Long brandId, ProductSortType sortType, Pageable pageable) {
        QProductModel product = QProductModel.productModel;

        BooleanBuilder condition = new BooleanBuilder();
        if(brandId != null) {
            condition.and(product.brand.id.eq(brandId));
        }

        Long total = queryFactory.select(product.id.countDistinct())
                .from(product)
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
                        product.likeCount
                ))
                .from(product)
                .where(condition)
                .orderBy(applySort(sortType, product))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<ProductLikeSummary> findProductDetail(Long productId) {
        QProductModel product = QProductModel.productModel;

        ProductLikeSummary result = queryFactory
                .select(Projections.constructor(
                        ProductLikeSummary.class,
                        product.id,
                        product.name,
                        product.brand.id,
                        product.brand.name,
                        product.price,
                        product.status,
                        product.likeCount
                ))
                .from(product)
                .where(
                        product.id.eq(productId)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    private OrderSpecifier<?> applySort(ProductSortType sortType, QProductModel product) {
        if (sortType == null || sortType == ProductSortType.DEFAULT) {
            return product.likeCount.desc();
        }

        return switch(sortType) {
            case LIKE_ASC -> product.likeCount.asc();
            case LIKE_DESC -> product.likeCount.desc();
            case PRICE_ASC -> product.price.asc();
            case PRICE_DESC -> product.price.desc();
            default -> product.likeCount.desc();
        };
    }
}
