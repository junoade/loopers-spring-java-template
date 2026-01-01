package com.loopers.batch.job.rankWeekly.step;

import com.loopers.batch.job.rankWeekly.step.dto.WeeklyAggRow;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class RankWeeklyProcessor implements ItemProcessor<WeeklyAggRow, WeeklyAggRow> {
    @Override
    public WeeklyAggRow process(WeeklyAggRow item) {
        return item;
    }
}
