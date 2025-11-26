package com.loopers.application;

import com.loopers.application.product.ProductQueryService;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.application.product.ProductLikeSummary;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductQueryServiceTest {
    @Autowired
    private ProductQueryService productQueryService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductLikeService productLikeService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("브랜드ID로 필터 및 좋아요 수 집계가 올바로 동작한다")
    void findProductLikes_withBrandFilter() {
        // given
        BrandModel brand = brandRepository.save(
                new BrandModel("브랜드A", "설명", BrandStatus.REGISTERED)
        );
        ProductModel p1 = productRepository.save(
                new ProductModel("상품1", "카테고리", 10_000, 100, ProductStatus.ON_SALE, brand)
        );
        ProductModel p2 = productRepository.save(
                new ProductModel("상품2", "카테고리", 20_000, 100, ProductStatus.ON_SALE, brand)
        );

        UserModel u1 = userRepository.save(new UserModel("user1", "u1", "유저1", "u1@test.com", "1997-01-01", "M", 100_000));
        UserModel u2 = userRepository.save(new UserModel("user2", "u2", "유저2", "u2@test.com", "1997-01-01", "M", 100_000));
        UserModel u3 = userRepository.save(new UserModel("user3", "u3", "유저3", "u3@test.com", "1997-01-01", "M", 100_000));

        productLikeService.like(u1.getId(), p1.getId());
        productLikeService.like(u2.getId(), p1.getId());
        productLikeService.like(u3.getId(), p2.getId());

        // when
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ProductLikeSummary> page = productQueryService.getProductListWithLikeCount(
                brand.getId(),
                ProductSortType.LIKE_DESC,
                pageable
        );

        // then
        assertThat(page.getContent()).hasSize(2);

        ProductLikeSummary first = page.getContent().get(0);
        ProductLikeSummary second = page.getContent().get(1);

        // 정렬: 좋아요 수 DESC 이므로 p1(2개) 먼저
        assertThat(first.getProductId()).isEqualTo(p1.getId());
        assertThat(first.getLikeCount()).isEqualTo(2L);

        assertThat(second.getProductId()).isEqualTo(p2.getId());
        assertThat(second.getLikeCount()).isEqualTo(1L);

        // groupBy에 brandId / brandName, price, status까지 들어간 게 잘 매핑되는지도 확인
        assertThat(first.getBrandId()).isEqualTo(brand.getId());
        assertThat(first.getBrandName()).isEqualTo(brand.getName());
        assertThat(first.getPrice()).isEqualTo(p1.getPrice());
        assertThat(first.getStatus()).isEqualTo(p1.getStatus());
    }

    @Test
    @DisplayName("상품 좋아요가 한 건도 없는 상품은 좋아요 개수가 0으로 조회된다")
    void findProductLikes_withBrandFilterButNoLikes() {
        // given
        BrandModel brand = brandRepository.save(
                new BrandModel("브랜드A", "설명", BrandStatus.REGISTERED)
        );
        ProductModel p1 = productRepository.save(
                new ProductModel("상품1", "카테고리", 10_000, 100, ProductStatus.ON_SALE, brand)
        );
        ProductModel p2 = productRepository.save(
                new ProductModel("상품2", "카테고리", 20_000, 100, ProductStatus.ON_SALE, brand)
        );

        UserModel u1 = userRepository.save(new UserModel("user1", "u1", "유저1", "u1@test.com", "1997-01-01", "M", 100_000));
        UserModel u2 = userRepository.save(new UserModel("user2", "u2", "유저2", "u2@test.com", "1997-01-01", "M", 100_000));
        UserModel u3 = userRepository.save(new UserModel("user3", "u3", "유저3", "u3@test.com", "1997-01-01", "M", 100_000));

        productLikeService.like(u1.getId(), p1.getId());

        // when
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ProductLikeSummary> page = productQueryService.getProductListWithLikeCount(
                brand.getId(),
                ProductSortType.LIKE_DESC,
                pageable
        );

        // then
        assertThat(page.getContent()).hasSize(2);

        ProductLikeSummary first = page.getContent().get(0);
        ProductLikeSummary second = page.getContent().get(1);

        // p1: likeCount = 1
        assertThat(first.getProductId()).isEqualTo(p1.getId());
        assertThat(first.getLikeCount()).isEqualTo(1L);

        // p2: 좋아요 없는 경우 0으로 집계
        assertThat(second.getProductId()).isEqualTo(p2.getId());
        assertThat(second.getLikeCount()).isEqualTo(0L);

    }

    @Test
    @DisplayName("특정 상품에 대한 세부 정보를 조회한다")
    void findProductDetail_withProductId() {
        // given
        BrandModel brand = brandRepository.save(
                new BrandModel("브랜드A", "설명", BrandStatus.REGISTERED)
        );
        ProductModel p1 = productRepository.save(
                new ProductModel("상품1", "카테고리", 10_000, 100, ProductStatus.ON_SALE, brand)
        );
        ProductModel p2 = productRepository.save(
                new ProductModel("상품2", "카테고리", 20_000, 100, ProductStatus.ON_SALE, brand)
        );

        UserModel u1 = userRepository.save(new UserModel("user1", "u1", "유저1", "u1@test.com", "1997-01-01", "M", 100_000));
        UserModel u2 = userRepository.save(new UserModel("user2", "u2", "유저2", "u2@test.com", "1997-01-01", "M", 100_000));
        UserModel u3 = userRepository.save(new UserModel("user3", "u3", "유저3", "u3@test.com", "1997-01-01", "M", 100_000));

        productLikeService.like(u1.getId(), p1.getId());
        productLikeService.like(u2.getId(), p2.getId());
        productLikeService.like(u3.getId(), p1.getId());

        // when
        ProductLikeSummary summary = productQueryService.getProductLikeSummary(p1.getId());

        // then
        assertThat(summary).isNotNull();
        assertThat(summary.getProductId()).isEqualTo(p1.getId());
        assertThat(summary.getProductName()).isEqualTo(p1.getName());
        assertThat(summary.getBrandId()).isEqualTo(p1.getBrand().getId());
        assertThat(summary.getBrandName()).isEqualTo(brand.getName());
        assertThat(summary.getPrice()).isEqualTo(p1.getPrice());
        assertThat(summary.getStatus()).isEqualTo(p1.getStatus());
        assertThat(summary.getLikeCount()).isEqualTo(2L);
    }
}
