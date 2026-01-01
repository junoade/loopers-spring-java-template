package com.loopers.batch.job.rankMonthly;

import com.loopers.batch.job.rankMonthly.dto.MonthlyAggRow;
import com.loopers.batch.job.rankMonthly.dto.MonthlyRankRow;
import com.loopers.batch.job.rankWeekly.step.dto.WeeklyAggRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankMonthlyProcessor implements ItemProcessor<MonthlyAggRow, MonthlyRankRow> {

    private final String yearMonth;

    @Override
    public MonthlyRankRow process(MonthlyAggRow item) {
        double score = calculateScore(item);
        return new MonthlyRankRow(
                yearMonth,
                item.productId(),
                item.viewCount(),
                item.likeCount(),
                item.orderCount(),
                score
        );
    }

    // TODO 정책적으로 분리할 수 있도록 개선한다
    private double calculateScore(MonthlyAggRow item) {
        return 0.1 * item.viewCount()
                + 0.2 * item.likeCount()
                + 0.7 * item.orderCount();
    }

}
