package com.loopers.infrastructure.ranking;

import com.loopers.domain.mv.ProductRankMonthly;
import com.loopers.domain.mv.ProductRankMonthlyId;
import com.loopers.ranking.RankingEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRankMonthlyRepository extends JpaRepository<ProductRankMonthly, ProductRankMonthlyId> {

    @Query("""
        select new com.loopers.ranking.RankingEntry(
            p.id.productId,
            p.score
        )
        from ProductRankMonthly p
        where p.id.yearMonth = :yearMonth
        order by p.score desc
    """)
    List<RankingEntry> findTopByYearMonth(@Param("yearMonth") String yearMonth, Pageable pageable);
}
