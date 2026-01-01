package com.loopers.batch.job.rankWeekly.step;

import com.loopers.batch.job.rankWeekly.step.dto.WeeklyAggRow;
import com.loopers.batch.job.rankWeekly.step.dto.WeeklyRankRow;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class RankWeeklyProcessor implements ItemProcessor<WeeklyAggRow, WeeklyRankRow> {
    @Override
    public WeeklyRankRow process(WeeklyAggRow item) {
        double score = calculateScore(item);
        return new WeeklyRankRow(
                item.yearWeek(),
                item.productId(),
                item.viewCount(),
                item.likeCount(),
                item.orderCount(),
                score
        );
    }

    // TODO 정책적으로 분리할 수 있도록 개선한다
    private double calculateScore(WeeklyAggRow item) {
        return 0.1 * item.viewCount()
                + 0.2 * item.likeCount()
                + 0.7 * item.orderCount();
    }
}
