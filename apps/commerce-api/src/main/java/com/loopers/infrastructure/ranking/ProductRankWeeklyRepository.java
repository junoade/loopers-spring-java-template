package com.loopers.infrastructure.ranking;

import com.loopers.domain.mv.ProductRankWeekly;
import com.loopers.domain.mv.ProductRankWeeklyId;
import com.loopers.ranking.RankingEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRankWeeklyRepository extends JpaRepository<ProductRankWeekly, ProductRankWeeklyId> {

    @Query("""
        select new com.loopers.ranking.RankingEntry(
            p.id.productId,
            p.score
        )
        from ProductRankWeekly p
        where p.id.yearWeek = :yearWeek
        order by p.score desc
    """)
    List<RankingEntry> findTopByYearWeek(@Param("yearWeek") String yearWeekKey, Pageable pageable);
}
